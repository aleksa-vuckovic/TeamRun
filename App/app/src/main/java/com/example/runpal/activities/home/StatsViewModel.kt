package com.example.runpal.activities.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.models.RunData
import com.example.runpal.repositories.run.CombinedRunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatsViewModel @Inject constructor(
    private val runRepository: CombinedRunRepository,
): ViewModel() {

    enum class State {
        LOADING, LOADED
    }

    private val _state = mutableStateOf(State.LOADING)
    private val _runData = mutableMapOf<String, List<RunData>>()
    private val _totalKm = mutableMapOf<String, Double>()
    private val _bestKm = mutableMapOf<String, Double>()
    private val _avgKm = mutableMapOf<String, Double>()
    private val _totalTime = mutableMapOf<String, Long>()
    private val _longestTime = mutableMapOf<String, Long>()

    val options = listOf("Week", "Month", "Year", "All time")
    val chartWidthMap = mapOf("Week" to 20f, "Month" to 15f, "Year" to 10f, "All time" to 5f)
    val state: State
        get() = _state.value
    val runData: Map<String, List<RunData>>
        get() = _runData
    val totalKm: Map<String, Double>
        get() = _totalKm
    val bestKm: Map<String, Double>
        get() = _bestKm
    val avgKm: Map<String, Double>
        get() = _avgKm
    val totalTime: Map<String, Long>
        get() = _totalTime
    val longestTime: Map<String, Long>
        get() = _longestTime

    init {
        viewModelScope.launch {
            val cur = System.currentTimeMillis()

            val times = listOf(7*24*60*60*1000L, 30*24*60*60*1000L, 365*24*60*60*1000L, cur)
            for (i in options.indices) {
                val key = options[i]
                val runs = runRepository.getRunsSince(cur-times[i])
                _totalKm[key] = runs.sumOf { it.location.distance }
                _bestKm[key] = runs.maxOfOrNull { it.location.distance } ?: 0.0
                _avgKm[key] = _totalKm[key]!!/(if (runs.size == 0) 1 else runs.size)
                _totalTime[key] = runs.sumOf { it.run.running }
                _longestTime[key] = runs.maxOfOrNull { it.run.running } ?: 0L
                _runData[key] = runs
            }
            _state.value = State.LOADED
        }
    }

}