package com.example.runpal.activities.running.solo

import android.content.Context
import android.graphics.BitmapFactory
import android.location.Location
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runpal.DEFAULT_ZOOM
import com.example.runpal.activities.running.LocalRunState
import com.example.runpal.activities.running.MapState
import com.example.runpal.R
import com.example.runpal.RUN_ID_KEY
import com.example.runpal.RUN_MARKER_COLORS
import com.example.runpal.RUN_MARKER_SIZE
import com.example.runpal.activities.running.LocalRunStateFactory
import com.example.runpal.activities.running.RunState
import com.example.runpal.getMarkerBitmap
import com.example.runpal.models.Run
import com.example.runpal.repositories.LoginManager
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoloRunViewModel @Inject constructor(
    private val localRunStateFactory: LocalRunStateFactory,
    private val loginManager: LoginManager,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _runState: LocalRunState
    private val _marker: MutableState<BitmapDescriptor>

    val mapState: MapState = MapState()
    val runState: RunState
        get() = _runState
    val marker: BitmapDescriptor
        get() = _marker.value

    init {
        val user = loginManager.currentUserId()!!
        val runId: Long = savedStateHandle[RUN_ID_KEY]!!
        _runState = localRunStateFactory.createLocalRunState(
            run = Run(user = user, id = runId),
            scope = viewModelScope
        )
        _marker = mutableStateOf(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.runner)
                .getMarkerBitmap(RUN_MARKER_SIZE, color = RUN_MARKER_COLORS[0])
            _marker.value = BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    fun updateLocation(loc: Location) {
        _runState.update(loc)
        viewModelScope.launch { mapState.adjustCamera(_runState.location) }
    }
    fun start() = _runState.start()
    fun pause() = _runState.pause()
    fun resume() = _runState.resume()
    fun end() = _runState.stop()
    fun centerSwitch() {
        mapState.centerToggle()
        viewModelScope.launch { mapState.adjustCamera(_runState.location, DEFAULT_ZOOM) }
    }

}