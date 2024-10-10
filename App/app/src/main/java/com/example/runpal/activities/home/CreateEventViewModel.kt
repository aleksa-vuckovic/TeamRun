package com.example.runpal.activities.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.MAX_IMAGE_SIZE
import com.example.runpal.R
import com.example.runpal.ServerException
import com.example.runpal.repositories.ServerEventRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val serverEventRepository: ServerEventRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    enum class State {
        INPUT, WAITING, SUCCESS
    }

    private val _state = mutableStateOf(State.INPUT)
    private val _error = mutableStateOf("")

    val state: State
        get() = _state.value
    val error: String
        get() = _error.value


    fun create(name: String, description: String, time: Long?, distance: Double, image: Uri?, path: List<LatLng>?, tolerance: Double?) {
        if (time == null) {
            _error.value = context.resources.getString(R.string.time_must_be_specified)
            return
        }
        _state.value = State.WAITING
        viewModelScope.launch {
            try {
                serverEventRepository.create(name = name, description = description, time = time, distance = distance, image = image, path = path, tolerance = tolerance)
                _state.value = State.SUCCESS
            } catch(e: ServerException) {
                _error.value = e.message ?: ""
                _state.value = State.INPUT
            } catch(e: Exception) {
                _error.value = context.resources.getString(R.string.no_internet_message)
                _state.value = State.INPUT
            }
        }
    }

}