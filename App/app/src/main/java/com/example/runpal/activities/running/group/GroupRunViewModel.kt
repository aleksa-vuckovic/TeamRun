package com.example.runpal.activities.running.group

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.DEFAULT_ZOOM
import com.example.runpal.activities.running.MapState
import com.example.runpal.R
import com.example.runpal.ROOM_ID_KEY
import com.example.runpal.RUN_MARKER_COLORS
import com.example.runpal.RUN_MARKER_SIZE
import com.example.runpal.ServerException
import com.example.runpal.activities.running.LocalRunState
import com.example.runpal.activities.running.LocalRunStateFactory
import com.example.runpal.activities.running.NonlocalRunStateFactory
import com.example.runpal.activities.running.RunState
import com.example.runpal.getBitmap
import com.example.runpal.getMarkerBitmap
import com.example.runpal.models.Room
import com.example.runpal.models.Run
import com.example.runpal.models.User
import com.example.runpal.repositories.LoginManager
import com.example.runpal.repositories.ServerRoomRepository
import com.example.runpal.repositories.user.CombinedUserRepository
import com.example.runpal.tryRepeat
import com.example.runpal.tryRepeatExp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupRunViewModel @Inject constructor(
    private val localRunStateFactory: LocalRunStateFactory,
    private val nonlocalRunStateFactory: NonlocalRunStateFactory,
    private val loginManager: LoginManager,
    private val serverRoomRepository: ServerRoomRepository,
    private val combinedUserRepository: CombinedUserRepository,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    enum class State {
        LOADING, LOADED, FAILED
    }

    private val _state = mutableStateOf(State.LOADING)
    private val _room = mutableStateOf(Room())
    private val _runStates = mutableStateListOf<RunState>()
    private val _markers = mutableStateListOf<BitmapDescriptor>()
    private val _users = mutableStateListOf<User>()


    val mapState: MapState = MapState()
    val state: State
        get() = _state.value
    val room: Room
        get() = _room.value
    val runStates: List<RunState>
        get() = _runStates
    val markers: List<BitmapDescriptor>
        get() = _markers
    val users: List<User>
        get() = _users

    init {
        viewModelScope.launch {
            /*
            The room data has to be loaded for the activity to start.
            Then, for each user, the runState, marker bitmap, and user data must be loaded.
            After the local user data has been loaded, the activity can start while the rest of
            the data can load in the background.
             */
            //First loading the bare minimum for the run to start
            val userId = loginManager.currentUserId()!!
            val roomID: String = savedStateHandle[ROOM_ID_KEY]!!
            val run = Run(user = userId, id = Run.UNKNOWN_ID, room = roomID)
            val localRunState = localRunStateFactory.createLocalRunState(run, viewModelScope)
            val runnerMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            try {
                val room = tryRepeat { serverRoomRepository.status(roomID) }
                val user = tryRepeat { combinedUserRepository.getUser(userId) }
                _room.value = room
                _runStates.add(localRunState)
                _markers.add(runnerMarker)
                _users.add(user)
                _state.value = State.LOADED
            } catch(e: ServerException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                _state.value = State.FAILED
                return@launch
            } catch(e: Exception) {
                Toast.makeText(context, context.resources.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
                _state.value = State.FAILED
                return@launch
            }

            //Load the actual runner bitmap
            with (Dispatchers.Default) {
                //switching to default dispatcher because bitmap manipulation can take some time??
                val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.runner)
                    .getMarkerBitmap(RUN_MARKER_SIZE, color = RUN_MARKER_COLORS[0])
                _markers[0] = BitmapDescriptorFactory.fromBitmap(bitmap)
            }

            //No get all the info for the rest of the users
            for (member in _room.value.members) {
                if (member == userId) continue
                var user: User
                var marker: BitmapDescriptor
                try {
                    user = tryRepeatExp { combinedUserRepository.getUser(member) }
                    with (Dispatchers.Default) {
                        val bitmap = user.profileUri.getBitmap(context.contentResolver)!!
                            .getMarkerBitmap(RUN_MARKER_SIZE, RUN_MARKER_COLORS[_users.size])
                        marker = BitmapDescriptorFactory.fromBitmap(bitmap)
                    }
                } catch (e: ServerException) {
                    e.printStackTrace()
                    //Failed to load. Continue with other users.
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    continue
                } catch(e: Exception) {
                    e.printStackTrace()
                    //Failed to load. Continue with other users.
                    Toast.makeText(context, context.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
                    //Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    continue
                }
                val runState = nonlocalRunStateFactory.createNonlocalRunState(
                    run = Run(user = member, id = Run.UNKNOWN_ID, room = _room.value._id),
                    scope = viewModelScope
                )
                _runStates.add(runState)
                _users.add(user)
                _markers.add(marker)
            }
        }
    }

    private val localRunState: LocalRunState
        get() = runStates[0] as LocalRunState

    fun updateLocation(loc: Location) {
        if (runStates.isEmpty()) return
        localRunState.update(loc)
        viewModelScope.launch { mapState.adjustCamera(localRunState.location) }
    }
    fun start() = localRunState.start()
    fun pause() = localRunState.pause()
    fun resume() = localRunState.resume()
    fun end() = localRunState.stop()
    fun centerSwitch() {
        mapState.centerToggle()
        viewModelScope.launch { mapState.adjustCamera(localRunState.location, DEFAULT_ZOOM) }
    }

}