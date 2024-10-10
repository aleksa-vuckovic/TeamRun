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
class HistoryViewModel @Inject constructor(
    private val runRepository: CombinedRunRepository
): ViewModel() {

    private var last = System.currentTimeMillis()
    private var loading = false

    private val _runs = mutableStateOf(listOf<RunData>())
    private val _end = mutableStateOf(false)

    val runs: List<RunData>
        get() = _runs.value
    val end: Boolean
        get() = _end.value

    init {
        more()
    }
    fun more() {
        if (_end.value) return
        viewModelScope.launch {
            if (loading) return@launch
            loading = true
            val batch = runRepository.getRuns(last, 10)
            _runs.value = _runs.value.plus(batch)
            _end.value = batch.isEmpty()
            last = batch.lastOrNull()?.run?.start ?: last
            loading = false
        }
    }
}