package com.example.runpal.activities.running.group

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.ROOM_ID_KEY
import com.example.runpal.ServerException
import com.example.runpal.models.Room
import com.example.runpal.models.User
import com.example.runpal.repositories.LoginManager
import com.example.runpal.repositories.ServerRoomRepository
import com.example.runpal.repositories.ServerUploadRepository
import com.example.runpal.repositories.user.CombinedUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.runpal.R

@HiltViewModel
class LobbyViewModel  @Inject constructor(
    private val serverRoomRepository: ServerRoomRepository,
    private val combinedUserRepository: CombinedUserRepository,
    loginManager: LoginManager,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
): ViewModel() {

    enum class State {
        LOADING, ERROR, WAITING, READY, START, LEAVE
    }
    companion object {
        val MAX_RETRIES = 10
    }

    val user: String = loginManager.currentUserId()!!

    private val _state = mutableStateOf(State.LOADING)
    private val _room = mutableStateOf(Room(_id = savedStateHandle[ROOM_ID_KEY]!!))
    private val _users = mutableStateMapOf<String, User>()

    val state: State
        get() = _state.value
    val room : Room
        get() = _room.value
    val users: Map<String, User>
        get() = _users



    init {
        viewModelScope.launch {
            var retries = 0
            while(true) {
                try {
                    val room = serverRoomRepository.status(_room.value._id)
                    val state =
                        if (room.start != null) State.START
                        else if (!room.ready.contains(user)) State.WAITING
                        else State.READY
                        for (id in _room.value.members)
                            if (!_users.containsKey(id))
                                _users[id] = combinedUserRepository.getUser(id)
                    _room.value = room
                    _state.value = state
                    retries = 0
                } catch (e: ServerException) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    _state.value = State.ERROR
                    break
                } catch (e: Exception) {
                    retries++
                    if (retries == MAX_RETRIES) {
                        Toast.makeText(context, context.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
                        _state.value = State.ERROR
                        break
                    }
                }
                delay(1000)
            }

        }

    }

    fun copy() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Room ID", _room.value._id)
        clipboard.setPrimaryClip(clip)
    }
    fun leave() {
        viewModelScope.launch {
            try {
                serverRoomRepository.leave(_room.value._id)
                _state.value = State.LEAVE
            } catch (e: Exception) {}
        }

    }

    fun ready() {
        viewModelScope.launch {
            try {
                serverRoomRepository.ready(_room.value._id)
            } catch (e: ServerException) {
                e.printStackTrace()
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, context.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
            }
        }
    }

}