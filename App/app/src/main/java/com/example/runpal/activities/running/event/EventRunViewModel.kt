package com.example.runpal.activities.running.event

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.DEFAULT_ZOOM
import com.example.runpal.EVENT_ID_KEY
import com.example.runpal.activities.running.MapState
import com.example.runpal.R
import com.example.runpal.RUN_MARKER_COLORS
import com.example.runpal.RUN_MARKER_SIZE
import com.example.runpal.ServerException
import com.example.runpal.activities.running.LocalRunState
import com.example.runpal.activities.running.LocalRunStateFactory
import com.example.runpal.activities.running.RunState
import com.example.runpal.getMarkerBitmap
import com.example.runpal.models.Event
import com.example.runpal.models.EventResult
import com.example.runpal.models.Run
import com.example.runpal.models.distance
import com.example.runpal.models.toLatLng
import com.example.runpal.models.toPathPoint
import com.example.runpal.models.toPoint3D
import com.example.runpal.repositories.LoginManager
import com.example.runpal.repositories.ServerEventRepository
import com.example.runpal.server.LiveRankingApi
import com.example.runpal.server.LiveRankingApiFactory
import com.example.runpal.tryRepeat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventRunViewModel @Inject constructor(
    private val loginManager: LoginManager,
    private val serverEventRepository: ServerEventRepository,
    private val savedStateHandle: SavedStateHandle,
    private val localRunStateFactory: LocalRunStateFactory,
    private val liveRankingApiFactory: LiveRankingApiFactory,
    @ApplicationContext private val context: Context
): ViewModel() {
    enum class State {
        LOADING, LOADED, FAILED
    }

    private val _state = mutableStateOf(State.LOADING)
    private val _event = mutableStateOf(Event())
    private val _runState: LocalRunState
    private val _marker: MutableState<BitmapDescriptor>
    private val _rankingLive: LiveRankingApi
    private val _warning = mutableStateOf<String?>(null)

    val mapState: MapState = MapState()
    val state: State
        get() = _state.value
    val event: Event
        get() = _event.value
    val runState: RunState
        get() = _runState
    val marker: BitmapDescriptor
        get() = _marker.value
    val rankingLive: List<EventResult>
        get() = _rankingLive.state
    val warning: String?
        get() = _warning.value

    init {
        val user = loginManager.currentUserId()!!
        val eventID: String = savedStateHandle[EVENT_ID_KEY]!!
        _rankingLive = liveRankingApiFactory.createLiveRankingApi(eventID)
        _rankingLive.start()
        _runState = localRunStateFactory.createLocalRunState(
            run = Run(user = user, id = Run.UNKNOWN_ID, event = eventID),
            scope = viewModelScope
        )
        _marker = mutableStateOf(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val event = tryRepeat { serverEventRepository.data(eventID) }
                _event.value = event
            } catch(e: ServerException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                _state.value = State.FAILED
                return@launch
            } catch(e: Exception) {
                Toast.makeText(context, context.resources.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
                _state.value = State.FAILED
                return@launch
            }
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.runner)
                .getMarkerBitmap(RUN_MARKER_SIZE, color = RUN_MARKER_COLORS[0])
            _marker.value = BitmapDescriptorFactory.fromBitmap(bitmap)
            _state.value = State.LOADED
        }
    }

    var lastUpdate: Long? = null
    val MAX_PENALTY = 600000 //10 meters for 60 seconds
    fun updateLocation(loc: Location) {
        var penalty = _runState.run.penalty ?: 0.0
        val path = event.path
        val point = loc.toLatLng()
        val curUpdate = System.currentTimeMillis()
        val interval = curUpdate - (lastUpdate?:curUpdate)
        val tolerance = event.tolerance ?: 50.0
        lastUpdate = curUpdate
        if (path != null) {
            _warning.value = null
            if (_runState.run.state == Run.State.RUNNING) {
                var cur = _runState.run.cur!!
                while(cur < path.size) {
                    //Check if the user is actually running along the specified path
                    val distance = distanceFromSegment(point, path.elementAtOrNull(cur-1) to path[cur])
                    if (distance > tolerance) {
                        _warning.value = "Get back on the event route!"
                        penalty += interval * (distance - tolerance)
                        _runState.eventPenalty(penalty)
                        if (penalty > MAX_PENALTY) {
                            abort()
                            return
                        }
                        break
                    }
                    val checkPointDistance = path[cur].distance(point)
                    if (checkPointDistance > 25) break
                    cur++
                }
                _runState.eventProgress(cur)
            }
        }

        _runState.update(loc)
        viewModelScope.launch { mapState.adjustCamera(_runState.location) }

        if (_runState.run.state == Run.State.RUNNING) {
            //Check end of event conditions
            if (path != null) {
                if (_runState.run.cur == path.size && _runState.location.distance >= event.distance)
                        finish()
            }
            else if (_runState.location.distance >= event.distance)
                finish()
        }
    }

    fun start() = _runState.start()

    fun finish() {
        _runState.stop()
    }
    fun abort() {
        _runState.stop(regular = false)
    }

    override fun onCleared() {
        _rankingLive.stop()
    }

    fun centerSwitch() {
        mapState.centerToggle()
        viewModelScope.launch { mapState.adjustCamera(_runState.location, DEFAULT_ZOOM) }
    }

    private fun distanceFromSegment(pointLL: LatLng, segmentLL: Pair<LatLng?, LatLng>): Double {
        val point = pointLL.toPoint3D()
        if (segmentLL.first == null) return pointLL.distance(segmentLL.second)
        val segment = segmentLL.first!!.toPoint3D() to segmentLL.second.toPoint3D()
        val vector = segment.second - segment.first
        if (vector.norm2() < 0.1) return (point-segment.second).norm()
        val intersection = vector * (vector * (point - segment.first) / vector.norm2()) + segment.first
        val direction = intersection - segment.first
        if (direction * vector < 0) return (point-segment.first).norm()
        else if (direction.norm() <= vector.norm()) return (point - intersection).norm()
        else return (point - segment.second).norm()
    }
}