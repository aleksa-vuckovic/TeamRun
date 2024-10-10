package com.example.runpal.activities.running

import android.content.Context
import android.location.Location
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.runpal.models.PathPoint
import com.example.runpal.filters.MovingAverageFilter
import com.example.runpal.filters.LocationFilter
import com.example.runpal.filters.SpeedFilter
import com.example.runpal.kcalExpenditure
import com.example.runpal.models.Run
import com.example.runpal.models.RunData
import com.example.runpal.models.toPathPoint
import com.example.runpal.repositories.run.CombinedRunRepository
import com.example.runpal.repositories.run.RunRepository
import com.example.runpal.repositories.user.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Represents the state of an ongoing run, including distance, kcal,
 * the entire path, time.
 */
interface RunState {
    val run: Run
    val location: PathPoint
    val path: List<PathPoint>
}

/**
 * A RunState which filters and manages GPS location data.
 * Used for activities on the host device.
 *
 * @param run Should contain the user and id fields. If the run already exists,
 * its state will be restored. Otherwise, the run will be in ready state, and
 * created once start() is invoked.
 * @param runRepository The run repository used to send updates, and also retrieve the existing run data during initialization.
 * @param scope The run state has its own timer, which needs a scope to run in.
 * Repository updates will also be launched in this scope.
 * THe scope must use the main dispatcher, to avoid concurrency issues.
 * @param updateInterval The average interval between two consecutive location updates,
 * used to determine the filter buffer sizes.
 */
class LocalRunState @AssistedInject constructor (@Assisted run: Run,
                                                 @Assisted private val scope: CoroutineScope,
                                                 private val runRepository: CombinedRunRepository,
                                                 private val userRepository: UserRepository
): RunState {
    private val _run = mutableStateOf(Run.LOADING)
    private val _location = mutableStateOf(PathPoint.INIT)
    private val _path: SnapshotStateList<PathPoint> = SnapshotStateList()
    private var userWeight: Double = 80.0

    init {
        scope.launch {
            try {
                userWeight = userRepository.getUser(run.user).weight
            } catch (e: Exception) {
                e.printStackTrace();/* User does not exist?! Crash. */ throw e
            }
            try {
                val existingData = runRepository.getUpdate(
                    user = run.user,
                    id = if (run.id == Run.UNKNOWN_ID) null else run.id,
                    room = run.room,
                    event = run.event
                )
                _run.value = existingData.run
                _location.value = PathPoint.init(
                    distance = existingData.location.distance,
                    kcal = existingData.location.kcal
                )
                _path.addAll(existingData.path)
                if (run.event == null) pause()
            } catch (e: Exception) {
                e.printStackTrace()
                /*It's a new run.*/
                _run.value = run.copy(
                    id = if (run.id == Run.UNKNOWN_ID) System.currentTimeMillis() else run.id,
                    start = null, running = 0L, end = null, paused = false, cur = if (run.event != null) 0 else null
                )
            }
        }
    }

    private val speedFilter = SpeedFilter(60000L)

    override val run: Run
        get() = _run.value
    override val location: PathPoint
        get() = _location.value
    override val path: List<PathPoint>
        get() = _path

    init {
        scope.launch {
            var prev = System.currentTimeMillis()
            while (true) {
                val cur = System.currentTimeMillis()
                if (_run.value.state == Run.State.RUNNING) _run.value =
                    _run.value.copy(running = _run.value.running + cur - prev)
                prev = cur
                delay(500)
            }
        }
    }


    /**
     * @param loc The current location (filtered).
     */
    fun update(loc: Location) {
        val cur = loc.toPathPoint()
        val runUpdate = RunData(run = _run.value, location = cur)

        val prev = _location.value
        cur.speed = prev.speed
        cur.distance = prev.distance
        cur.kcal = prev.kcal
        if (_run.value.state == Run.State.RUNNING) {
            if (!prev.isInit()) {
                val distanceDifference = prev.distance(cur)
                val timeDifference = (cur.time - prev.time) / 1000
                cur.distance += distanceDifference
                cur.speed = speedFilter.filter(cur)
                val slope =
                    if (distanceDifference != 0.0) (cur.altitude - prev.altitude) / distanceDifference else 0.0
                val expenditure = kcalExpenditure(cur.speed, slope, userWeight)
                cur.kcal += expenditure * timeDifference
            }
            _path.add(cur)
            runUpdate.path = listOf(cur)
        }
        _location.value = cur
        if (_run.value.state != Run.State.READY && _run.value.state != Run.State.LOADING)
            scope.launch { runRepository.update(runUpdate) }
    }

    fun start() {
        if (_run.value.state != Run.State.READY) return
        _run.value = _run.value.copy(
            start = System.currentTimeMillis(),
            paused = false,
            id = if (_run.value.id == Run.UNKNOWN_ID) System.currentTimeMillis() else _run.value.id
        )
        speedFilter.clear(_location.value)
        scope.launch { runRepository.create(_run.value) }
    }

    fun pause() {
        if (_run.value.state != Run.State.RUNNING) return
        _run.value = _run.value.copy(paused = true)
        val update = getForceUpdate()
        scope.launch { runRepository.update(update) }
    }

    fun resume() {
        if (_run.value.state != Run.State.PAUSED) return
        _run.value = _run.value.copy(paused = false)
        speedFilter.clear(_location.value)
    }

    fun stop(regular: Boolean = true) {
        if (_run.value.state == Run.State.READY
            || _run.value.state == Run.State.ENDED
            || _run.value.state == Run.State.LOADING
        ) return
        _run.value = _run.value.copy(end = System.currentTimeMillis())
        val update = getForceUpdate(end = regular)
        scope.launch { runRepository.update(update) }
    }

    fun eventProgress(cur: Int) {
        _run.value = _run.value.copy(cur = cur)
    }

    fun eventPenalty(value: Double) {
        _run.value = _run.value.copy(penalty = value)
    }

    private fun getForceUpdate(end: Boolean = true): RunData {
        val update = RunData(run = _run.value, location = _location.value, path = listOf())
        val last = _path.lastOrNull()
        if (last == null || last.end != end) {
            val fakeend = if (last == null) _location.value.copy(end = end) else last.copy(
                end = end,
                time = last.time + 1
            )
            _path.add(fakeend)
            update.path = listOf(fakeend)
        }
        return update
    }
}

/**
 * A RunState which is used for data obtained from the server,
 * tracked by another device.
 *
 * @param run A run object containing some valid pair of identifying fields.
 * (id+user, room+user, event+user)
 * If id is not to be used, set to -1.
 * @param scope A scope for the fetcher coroutine.
 * @param interval The time in millis between two update fetches.
 * @param runRepository The repository to fetch from.
 */
class NonlocalRunState @AssistedInject constructor (
    @Assisted run: Run,
    @Assisted scope: CoroutineScope,
    @Assisted private val interval: Long,
    private val runRepository: RunRepository,
    @ApplicationContext val context: Context
): RunState {
    private val _run = mutableStateOf(Run.LOADING)
    private val _location: MutableState<PathPoint> = mutableStateOf(PathPoint.INIT)
    private val _path: SnapshotStateList<PathPoint> = SnapshotStateList()

    private var lastFetch: Long = 0L

    override val run: Run
        get() = _run.value
    override val location: PathPoint
        get() = _location.value
    override val path: List<PathPoint>
        get() = _path

    init {
        scope.launch {
            while(true) {
                try {
                    val update = runRepository.getUpdate(
                        user = run.user,
                        id = if (run.id == Run.UNKNOWN_ID) null else run.id,
                        room = run.room,
                        event = run.event,
                        since = lastFetch
                    )
                    _run.value = update.run
                    _location.value = update.location
                    _path.addAll(update.path)
                    if (update.path.size > 0) lastFetch = update.path.last().time
                } catch(_: Exception) { }
                delay(interval)
            }
        }
    }
}

@AssistedFactory
interface LocalRunStateFactory {
    fun createLocalRunState(run: Run,
                            scope: CoroutineScope): LocalRunState
}
@AssistedFactory
interface NonlocalRunStateFactory {
    fun createNonlocalRunState(run: Run,
                               scope: CoroutineScope,
                               interval: Long = 2000L): NonlocalRunState
}
