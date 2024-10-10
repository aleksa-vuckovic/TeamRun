package com.example.runpal.activities.running

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runpal.BitmapDescriptorStore
import com.example.runpal.CenteredMarker
import com.example.runpal.GoogleMapPath
import com.example.runpal.KcalFormatter
import com.example.runpal.LongTimeFormatter
import com.example.runpal.MAP_BUTTON_COLOR
import com.example.runpal.MAP_ID
import com.example.runpal.ProgressFloatingButton
import com.example.runpal.TimeFormatter
import com.example.runpal.Units
import com.example.runpal.borderRight
import com.example.runpal.models.toLatLng
import com.example.runpal.ui.theme.TransparentWhite
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.delay
import com.example.runpal.R
import com.example.runpal.models.Event
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polyline
import com.google.maps.android.ktx.buildGoogleMapOptions


@Composable
fun PanelText(text: Pair<String,String>,
              bigStyle: TextStyle = MaterialTheme.typography.labelLarge,
              smallStyle: TextStyle = MaterialTheme.typography.labelSmall,
              modifier: Modifier = Modifier) {
    var subscriptOffset: Float
    LocalDensity.current.run { subscriptOffset = bigStyle.fontSize.toPx() / 2 }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = text.first,
            style = bigStyle)
        Text(
            text = text.second,
            style = smallStyle,
            modifier = Modifier.graphicsLayer {
                this.translationY = subscriptOffset
            })
    }
}

@Composable
fun RunDataPanel(runState: RunState,
                 units: Units,
                 onChangeUnits: () -> Unit,
                 pace: Boolean,
                 onChangePace: () -> Unit,
                 modifier: Modifier = Modifier) {


    val big = MaterialTheme.typography.titleSmall.copy(fontSize = 22.sp)
    val small = MaterialTheme.typography.labelMedium
    Column(modifier = modifier) {
        Row(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            PanelText(text = units.distance.formatter.format(runState.location.distance),
                modifier = Modifier
                    .fillMaxSize()
                    .borderRight(1.dp, Color.LightGray)
                    .weight(1f)
                    .clickable { onChangeUnits() }
                    .padding(vertical = 20.dp),
                bigStyle = big,
                smallStyle = small
            )
            PanelText(text = KcalFormatter.format(runState.location.kcal),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(vertical = 20.dp),
                bigStyle = big,
                smallStyle = small
            )
        }
        Divider(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
        )
        Row(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            PanelText(text = if (pace) units.pace.formatter.format(runState.location.speed)
                        else units.speed.formatter.format(runState.location.speed),
                modifier = Modifier
                    .fillMaxSize()
                    .borderRight(1.dp, Color.LightGray)
                    .weight(1f)
                    .clickable { onChangePace() }
                    .padding(vertical = 20.dp),
                bigStyle = big,
                smallStyle = small
            )
            PanelText(text = TimeFormatter.format(runState.run.running),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(vertical = 20.dp),
                bigStyle = big,
                smallStyle = small
            )
        }
    }
}

@Composable
fun RunStart(onStart: () -> Unit, sound: MediaPlayer? = null) {
    var countdown by remember {
        mutableStateOf("")
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = if (countdown == "") Color.Transparent else Color.White.copy(alpha = 0.75f))) {

        if (countdown != "")
            Text(text = countdown,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.displayLarge)

        ProgressFloatingButton(
            onProgress = {
                if (it == 0f) countdown = ""
                else if (it >= 1f) onStart()
                else {
                    val prev = countdown
                    countdown = (4f - it*4f).toInt().toString()
                    if (countdown != prev && prev != "") sound?.start()
                }
            },
            time = 4000L,
            color = Color.Green,
            modifier = Modifier
                .padding(bottom = 10.dp)
                .size(90.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(stringResource(id = R.string.start))
        }
    }
}
@Composable
fun RunPause(onPause: (() -> Unit)? = null, onFinish: (() -> Unit)? = null) {
    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        if (onPause != null) ProgressFloatingButton(
            onComplete = onPause,
            time = 2000L,
            color = Color.Gray,
            modifier = Modifier
                .padding(start = 10.dp, bottom = 10.dp)
                .size(70.dp)
                .align(Alignment.BottomStart)
        ) {
            Icon(imageVector = Icons.Default.Pause, contentDescription = stringResource(id = R.string.pause), modifier = Modifier.size(40.dp))
        }
        if (onFinish != null) ProgressFloatingButton(
            onComplete = onFinish,
            time = 2000L,
            color = Color.Red,
            modifier = Modifier
                .padding(start = if (onPause == null) 10.dp else 90.dp, bottom = 10.dp)
                .size(70.dp)
                .align(Alignment.BottomStart)

        ) {
            Text(stringResource(id = R.string.finish))
        }
    }
}
@Composable
fun RunResume(onResume: () -> Unit, onFinish: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = Color.White.copy(alpha = 0.75f))
    ) {
        Icon(imageVector = Icons.Filled.Pause,
            contentDescription = "Paused",
            tint = Color.LightGray,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center))
        ProgressFloatingButton(
            onComplete = onResume,
            time = 2000L,
            color = Color.Yellow,
            modifier = Modifier
                .padding(start = 10.dp, bottom = 10.dp)
                .size(70.dp)
                .align(Alignment.BottomStart)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = stringResource(id = R.string.resume), modifier = Modifier.size(40.dp))
        }
        ProgressFloatingButton(
            onComplete = onFinish,
            time = 2000L,
            color = Color.Red,
            modifier = Modifier
                .padding(start = 90.dp, bottom = 10.dp)
                .size(70.dp)
                .align(Alignment.BottomStart)
        ) {
            Text(stringResource(id = R.string.finish))
        }
    }
}

@Composable
fun RunCountown(till: Long, onStart: () -> Unit, sound: MediaPlayer? = null) {
    var countdown by rememberSaveable {
        mutableStateOf("")
    }
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = TransparentWhite)) {
        Text(text = countdown,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.displayLarge)
    }
    LaunchedEffect(key1 = till) {
        while(true) {
            val left = till - System.currentTimeMillis()
            if (left <= 0) onStart()
            else {
                val prev = countdown
                if (left > 10*60*1000) countdown = LongTimeFormatter.format(left).first
                else if (left > 60*1000) countdown = TimeFormatter.format(left).first
                else {
                    countdown = (left/1000L).toString()
                    if (countdown != prev && prev != "") sound?.start()
                }
            }
            delay(200)
        }
    }
}

@Composable
fun GoogleMapRun(runStates: List<RunState>,
                 markers: List<BitmapDescriptor>,
                 colors: List<Color>,
                 mapState: MapState,
                 onCenterSwitch: () -> Unit,
                 event: Event? = null,
                 modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = mapState.cameraPositionState,
            googleMapOptionsFactory = { buildGoogleMapOptions { mapId(MAP_ID) } }
        ) {
            if (event?.path != null)
                GoogleMapEventPath(path = event.path, complete = true)
            for (i in runStates.indices) {
                GoogleMapPath(pathPoints = runStates[i].path, color = colors[i])
            }
            for (i in runStates.indices) {
                if (!runStates[i].location.isInit()) Marker(
                    state = MarkerState(position = runStates[i].location.toLatLng()),
                    icon = markers[i],
                    anchor = Offset(0.5f, 0.5f)
                )
            }
        }
        IconButton(
            onClick = onCenterSwitch,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 9.dp, bottom = 100.dp)
                .background(
                    color = MAP_BUTTON_COLOR,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (mapState.centered) Icons.Default.CropFree else Icons.Default.CenterFocusStrong,
                contentDescription = "Recenter/Decenter"
            )
        }
    }
}

@Composable
fun GoogleMapEventPath(path: List<LatLng>, complete: Boolean = false) {
    Polyline(points = path, color = MaterialTheme.colorScheme.inversePrimary, width = 20f, visible = true)
    Polyline(points = path, color = MaterialTheme.colorScheme.primary, width = 10f, visible = true)
    if (complete && path.isNotEmpty()) {
        CenteredMarker(latLng = path.first(), icon = BitmapDescriptorStore.getCircle(MaterialTheme.colorScheme.primary))
        Marker(state = MarkerState(path.last()), icon = BitmapDescriptorStore.getCheckerFlag(60, 45), anchor = Offset(0f, 1f))
    }
}