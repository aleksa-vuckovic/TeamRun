package com.example.runpal.activities.results.group

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.ROOM_ID_KEY
import com.example.runpal.ServerException
import com.example.runpal.models.RunData
import com.example.runpal.models.User
import com.example.runpal.repositories.ServerRoomRepository
import com.example.runpal.repositories.run.CombinedRunRepository
import com.example.runpal.repositories.user.CombinedUserRepository
import com.example.runpal.tryRepeat
import com.example.runpal.PathChartDataset
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.runpal.R

@HiltViewModel
class GroupRunResultsViewModel @Inject constructor(
    private val roomRepository: ServerRoomRepository,
    private val userRepository: CombinedUserRepository,
    private val runRepository: CombinedRunRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
): ViewModel() {
    enum class State {
        LOADING, LOADED, ERROR
    }
    private val _state = mutableStateOf(State.LOADING)
    private val _users = mutableStateOf( listOf<User>())
    private val _runs = mutableStateOf(listOf<RunData>())
    private val _speedDatasets = mutableStateOf(listOf<PathChartDataset>())
    private val _kcalDatasets = mutableStateOf(listOf<PathChartDataset>())
    private val _altitudeDatasets = mutableStateOf(listOf<PathChartDataset>())
    private val _distanceDatasets = mutableStateOf(listOf<PathChartDataset>())

    val state: State
        get() = _state.value
    val users: List<User>
        get() = _users.value
    val runs: List<RunData>
        get() = _runs.value
    val speedDatasets: List<PathChartDataset>
        get() = _speedDatasets.value
    val kcalDatasets: List<PathChartDataset>
        get() = _kcalDatasets.value
    val altitudeDatasets: List<PathChartDataset>
        get() = _altitudeDatasets.value
    val distanceDatasets: List<PathChartDataset>
        get() = _distanceDatasets.value


    val roomID: String = savedStateHandle[ROOM_ID_KEY]!!

    init {
        reload()
    }

    fun reload() {
        _state.value = State.LOADING
        viewModelScope.launch {
            try {
                val room = tryRepeat { roomRepository.status(roomID) }
                val users = mutableListOf<User>()
                val runs = mutableListOf<RunData>()
                val speedDatasets = mutableListOf<PathChartDataset>()
                val kcalDatasets = mutableListOf<PathChartDataset>()
                val altitudeDatasets = mutableListOf<PathChartDataset>()
                val distanceDatasets = mutableListOf<PathChartDataset>()

                for (id in room.members) {
                    val user = tryRepeat { userRepository.getUser(id) }
                    val run = tryRepeat { runRepository.getUpdate(user = id, id = null, room = roomID) }
                    val speedDataset = PathChartDataset(data = run.path, xValue = {it.time.toDouble()}, yValue = {it.speed})
                    val kcalDataset = PathChartDataset(data = run.path, xValue = {it.time.toDouble()}, yValue = {it.kcal})
                    val altitudeDataset = PathChartDataset(data = run.path, xValue = {it.time.toDouble()}, yValue = {it.altitude})
                    val distanceDataset = PathChartDataset(data = run.path, xValue = {it.time.toDouble()}, yValue = {it.distance})
                    users.add(user)
                    runs.add(run)
                    speedDatasets.add(speedDataset)
                    kcalDatasets.add(kcalDataset)
                    altitudeDatasets.add(altitudeDataset)
                    distanceDatasets.add(distanceDataset)
                }

                _users.value = users
                _runs.value = runs
                _speedDatasets.value = speedDatasets
                _kcalDatasets.value = kcalDatasets
                _altitudeDatasets.value = altitudeDatasets
                _distanceDatasets.value = distanceDatasets
                _state.value = State.LOADED
            } catch(e: ServerException) {
                e.printStackTrace()
                _state.value = State.ERROR
            } catch(e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, context.resources.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
                _state.value = State.ERROR
            }

        }
    }
}