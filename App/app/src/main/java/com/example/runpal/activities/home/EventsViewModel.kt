package com.example.runpal.activities.home

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.R
import com.example.runpal.models.Event
import com.example.runpal.repositories.ServerEventRepository
import com.example.runpal.tryRepeat
import com.example.runpal.tryRepeatExp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EventsViewModel @Inject constructor(
    private val serverEventRepository: ServerEventRepository,
    @ApplicationContext private val context: Context
): ViewModel() {

    enum class State {
        LOADING, LOADED
    }

    private val _state: MutableState<State> = mutableStateOf(State.LOADING)
    private val _followedEvents: MutableState<List<Event>> = mutableStateOf(listOf())
    private val _otherEvents: MutableState<List<Event>> = mutableStateOf(listOf())

    val state: State
        get() = _state.value
    val followedEvents: List<Event>
        get() = _followedEvents.value
    val otherEvents: List<Event>
        get() = _otherEvents.value

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _state.value = State.LOADING
            try {
                _followedEvents.value = tryRepeat { serverEventRepository.find(following = true) }
                _otherEvents.value = tryRepeat { serverEventRepository.find(following = false) }
            } catch(e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, context.resources.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
            }
            _state.value = State.LOADED
        }
    }

    fun search(term: String) {
        viewModelScope.launch {
            try {
                _otherEvents.value = tryRepeat { serverEventRepository.find(search = term, following = false) }
            } catch(e: Exception) {
                Toast.makeText(context, context.resources.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
            }
        }
    }



}