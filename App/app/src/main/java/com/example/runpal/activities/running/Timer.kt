package com.example.runpal.activities.running

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class Timer @Inject constructor() {
    enum class State {
        READY,
        RUNNING,
        PAUSED,
        ENDED
    }
    private val scope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main)
    private val _state: MutableState<State> = mutableStateOf(State.READY)
    private val _time: MutableState<Long> = mutableStateOf(0L)
    init {
        scope.launch {
            var prev = System.currentTimeMillis()
            while(true) {
                val cur = System.currentTimeMillis()
                if (state == State.RUNNING) _time.value += cur - prev
                prev = cur
                delay(500)
            }
        }
    }




    val state: State
        get() = _state.value
    val time: Long
        get() = _time.value
    var startTime: Long = 0L
        private set
    var endTime: Long = 0L
        private set

    fun stateAsState(): androidx.compose.runtime.State<State> = _state
    fun timeAsState(): androidx.compose.runtime.State<Long> = _time

    fun start() {
        if (state != State.READY) throw IllegalStateException("Timer is already started.")
        startTime = System.currentTimeMillis()
        _state.value = State.RUNNING
    }
    fun pause() {
        if (state != State.RUNNING) throw IllegalStateException("Timer is not running.")
        _state.value = State.PAUSED
    }
    fun resume() {
        if (state != State.PAUSED) throw IllegalStateException("Timer is not paused.")
        _state.value = State.RUNNING
    }
    fun stop() {
        if (state == State.READY || state == State.ENDED) throw IllegalStateException("Timer is not running.")
        endTime = System.currentTimeMillis()
        _state.value = State.ENDED
    }
    fun reset() {
        _state.value = State.READY
        _time.value = 0L
        startTime = 0L
        endTime = 0L
    }

}