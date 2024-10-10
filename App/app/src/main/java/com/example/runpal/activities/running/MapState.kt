package com.example.runpal.activities.running

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.runpal.models.PathPoint
import com.example.runpal.models.toLatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.CancellationException


class MapState(centered: Boolean = true) {
    private val _centered: MutableState<Boolean> = mutableStateOf(true)

    val cameraPositionState: CameraPositionState = CameraPositionState(CameraPosition.fromLatLngZoom(
        LatLng(44.8195, 20.4423), 15f
    ))
    val centered: Boolean
        get() = _centered.value

    init {
        _centered.value = centered
    }

    suspend fun adjustCamera(pathPoint: PathPoint, zoom: Float? = null) {
        if (_centered.value)
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(
                            pathPoint.toLatLng(),
                            if (zoom == null) cameraPositionState.position.zoom else zoom
                        )
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is CancellationException)
                    throw e
            }
    }
    fun centerToggle() {
        _centered.value = !_centered.value
    }
}