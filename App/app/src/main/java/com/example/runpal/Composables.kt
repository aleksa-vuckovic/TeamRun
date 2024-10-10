package com.example.runpal

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.runpal.models.PathPoint
import com.example.runpal.models.toLatLng
import com.example.runpal.ui.theme.DarkPink
import com.example.runpal.ui.theme.DarkPurple
import com.example.runpal.ui.theme.StandardSpinner
import com.example.runpal.ui.theme.StandardTextField
import com.example.runpal.ui.theme.TransparentWhite
import com.example.runpal.ui.theme.YellowGreen
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Float.min
import kotlin.math.sqrt


@Composable
fun Modifier.borderRight(strokeWidth: Dp = 1.dp, color: Color = Color.Black): Modifier {
    val density = LocalDensity.current
    val strokeWidthPx = density.run { strokeWidth.toPx() }

    return this.drawBehind {
        val width = size.width - strokeWidthPx/2
        val height = size.height

        drawLine(
            color = color,
            start = Offset(x = width, y = 0f),
            end = Offset(x = width , y = height),
            strokeWidth = strokeWidthPx
        )
    }
}
@Composable
fun Modifier.borderBottom(strokeWidth: Dp = 1.dp, color: Color = Color.Black, outer: Boolean = false): Modifier {
    val density = LocalDensity.current
    val strokeWidthPx = density.run { strokeWidth.toPx() }

    return this.drawBehind {
        val width = size.width
        val height = size.height - (if (outer) 0f else strokeWidthPx/2)

        drawLine(
            color = color,
            start = Offset(x = 0f, y = height),
            end = Offset(x = width , y = height),
            strokeWidth = strokeWidthPx
        )
    }
}

@Composable
fun Modifier.ripple(duration: Int = 500, color: Color = Color.Gray.copy(alpha = 0.5f)): Modifier {
    var pressed: Boolean? by remember { mutableStateOf(null) }
    var position: Offset by remember {mutableStateOf(Offset.Unspecified)}
    var radius by remember { mutableStateOf(0f) }
    var start by remember {mutableStateOf(1f)}
    var end by remember {mutableStateOf(1f)}
    LaunchedEffect(key1 = pressed) {
        if (pressed == true) {
            start = 0f;
            end = 0f;
        }
        else if (pressed == false) {
            val innerDelay = 0.25f
            val time = System.currentTimeMillis()
            do {
                val cur = System.currentTimeMillis()
                val phase = (cur - time) / duration.toFloat()
                end = phase * 2
                if (phase > innerDelay)
                    start = (phase - innerDelay) / (1 - innerDelay)
                if (end > 1)
                    end = 1f
                delay(20)
            } while (duration > cur - time)
            start = 1f
            end = 1f
        }
    }
    return this.then(
        Modifier
            .pointerInput(null) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> {
                                pressed = true; position = event.changes.first().position;
                            }

                            PointerEventType.Release -> pressed = false
                        }
                    }
                }
            }
            .onSizeChanged { radius = sqrt(it.width * it.width + it.height * it.height.toFloat()) }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.radialGradient(
                        start to Color.Transparent,
                        end to color,
                        tileMode = TileMode.Clamp,
                        center = position,
                        radius = radius
                    ),
                    size = size,
                    topLeft = Offset.Zero
                )
            }
    )
}

fun Modifier.trapezoidBackground(
    color: Color,
    topLeft: Float = 0f,
    topRight: Float = 0f,
    bottomLeft: Float = 0f,
    bottomRight: Float = 0f
) = this.then(
    Modifier.drawBehind {
        val path = Path().apply {
            moveTo(topLeft*size.width, 0f)
            lineTo(size.width*(1 - topRight), 0f)
            lineTo(size.width*(1 - bottomRight), size.height)
            lineTo(bottomLeft*size.width, size.height)
            close()
        }
        drawPath(path, color, style = Fill)
    }
)

@Composable
fun ProgressFloatingButton(onProgress: ((Float) -> Unit)? = null,
                           onComplete: (() -> Unit)? = null,
                           time: Long,
                           color: Color = Color.Gray,
                           modifier: Modifier = Modifier,
                           content: @Composable () -> Unit) {

    var size by remember {
        mutableStateOf(Size(0f, 0f))
    }
    val diameter = remember(size) {
        min(size.width, size.height)
    }
    val center = remember(size) {
        Offset(size.width/2, size.height/2)
    }
    val topLeft = remember(size) {
        Offset(center.x - diameter/2, center.y - diameter/2)
    }
    val buttonSize = LocalDensity.current.run {
        (diameter*0.8f).toDp()
    }
    var pressed by remember {
        mutableStateOf(0L)
    }
    var progress by remember {
        mutableStateOf(0f)
    }
    LaunchedEffect(pressed) {
        progress = 0f
        if (pressed != 0L)
            while (true) {
                if (pressed == 0L) throw CancellationException()
                progress = (System.currentTimeMillis() - pressed) / time.toFloat()
                onProgress?.invoke(progress)
                if (progress >= 1f) {
                    onComplete?.invoke()
                    break
                }
                delay(30)
            }
        else
            onProgress?.invoke(0f)
    }
    Box(modifier = modifier
        .onSizeChanged { size = it.toSize() }
        .drawBehind {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = true,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Fill
            )
            drawCircle(
                color = color,
                center = center,
                radius = diameter / 2,
                style = Stroke(width = 4f)
            )
        }) {
        FloatingActionButton(onClick = {  },
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(buttonSize)
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            if (event.type == PointerEventType.Press) pressed =
                                System.currentTimeMillis()
                            else if (event.type == PointerEventType.Release)
                                pressed = 0L
                        }
                    }
                },
            shape = CircleShape) {
            content()
        }
    }

}

@Composable
fun DoubleInput(value: Double, onChange: (Double) -> Unit, enabled: Boolean = false, modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf<Pair<Double?, String>>(null to "") }
    var candidate by remember { mutableStateOf<Pair<Double?, String>>(null to "") }
    if (value == candidate.first)
        input = candidate
    val display = if (input.first == value) input.second else "%.3f".format(value)
    StandardTextField(
        value = display,
        onChange = {
            val v = if (it.isEmpty()) 0.0 else it.toDoubleOrNull()
            if (v != null) {
                candidate = v to it
                onChange(v)
            }
        },
        enabled = enabled,
        modifier = modifier
    )
}

@Composable
fun DoubleInputWithUnit(value: Double, onChange: (Double) -> Unit, enabled: Boolean = false, unit: String, onChangeUnit: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        DoubleInput(
            value = value,
            enabled = enabled,
            onChange = onChange,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )
        Box(modifier = Modifier
            .fillMaxHeight()
            .width(55.dp)
            .clickable { onChangeUnit() }
            .background(color = MaterialTheme.colorScheme.surface)
            .borderBottom(1.dp, MaterialTheme.colorScheme.onSurface)) {
            Text(
                text = unit,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImageSelector(input: Uri?, onSelect: (Uri?) -> Unit, modifier: Modifier = Modifier) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            onSelect(it)
        }
    }

    Box(
        modifier = modifier
            .clickable {
                launcher.launch("image/*")
            },
        contentAlignment = Alignment.Center
    ) {
        if (input != null) {
            Image(
                painter = rememberImagePainter(data = input),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.ImageSearch,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun LoadingDots(size: Dp, count: Int, color: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(size * 2 / 3),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
    ) {
        val time = timeAsState(start = System.currentTimeMillis())
        for (i in 0..count - 1)
            Box(
                modifier = Modifier
                    .padding(
                        bottom = waveFloat(
                            0f,
                            size.value * 4 / 3,
                            3000L,
                            -i * 200L,
                            size.value / 3,
                            time.value
                        ).dp
                    )
                    .size(size)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )
    }
}

@Composable
fun LoadingScreen(dotSize: Dp = 30.dp) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = TransparentWhite),
        contentAlignment = Alignment.Center
    ) {
        LoadingDots(size = dotSize, count = 3)
    }
}

@Composable
fun ErrorScreen(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .fillMaxSize()
        .background(color = TransparentWhite),
        contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
    }
}

@Composable
fun FlashingErrorScreen(message: String) {
    var alpha by remember {
        mutableStateOf(0.4f)
    }
    LaunchedEffect(key1 = null) {
        val start = System.currentTimeMillis()
        val period = 1000L
        while(true) {
            val cur = (System.currentTimeMillis() - start) % period
            if (cur > period/2) alpha = 0.8f - 0.4f * (2*cur/period.toFloat() - 1)
            else alpha = 0.4f + 0.4f * (2*cur/period.toFloat())
            delay(25)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Red.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center, color = Color.Black)
    }
}

@Composable
fun CenteredMarker(latLng: LatLng, icon: BitmapDescriptor) {
    Marker(
        state = MarkerState(position = latLng),
        icon = icon,
        anchor = Offset(0.5f, 0.5f)
    )
}


fun List<PathPoint>.toSegments(): List<List<LatLng>> {
    val res: MutableList<List<LatLng>> = mutableListOf()
    var cur: MutableList<LatLng> = mutableListOf()
    for (p in this) {
        cur.add(p.toLatLng())
        if (p.end) {
            res.add(cur)
            cur = mutableListOf()
        }
    }
    if (!cur.isEmpty()) res.add(cur)
    return res
}
@Composable
fun GoogleMapPath(pathPoints: List<PathPoint>,
                  startColor: Color = Color.Green,
                  color: Color,
                  endColor: Color = Color.Red) {
    val segments = remember(pathPoints, pathPoints.size) {pathPoints.toSegments()}
    for (i in segments.indices) {
        if (i == 0) CenteredMarker(latLng = segments[i].first(), icon = BitmapDescriptorStore.getCircle(startColor))
        else CenteredMarker(latLng = segments[i].first(), icon = BitmapDescriptorStore.getCircle(color))
        Polyline(points = segments[i], color = color, width = 10f, visible = true)
        if (i != segments.size - 1) CenteredMarker(latLng = segments[i].last(), icon = BitmapDescriptorStore.getCircle(color))
        else if (pathPoints.lastOrNull()?.end == true) CenteredMarker(latLng = segments[i].last(), icon = BitmapDescriptorStore.getCircle(endColor))
    }
}


@Composable
fun risingDoubleAsState(target: Double, onComplete: (() -> Unit)? = null ): State<Double> {
    val cur = rememberSaveable {
        mutableStateOf(0.0)
    }
    LaunchedEffect(key1 = target) {
        val duration = 2000
        var time = 0
        while(true) {
            delay(50)
            time += 51
            val x = time.toDouble()/duration
            if (x >= 1) {
                cur.value = target
                break
            }
            cur.value = Math.pow(x, 0.2)*target
        }
        if (onComplete != null) onComplete()
    }
    return cur
}
@Composable
fun risingLongAsState(target: Long): State<Long> {
    var finished by rememberSaveable(target) {
        mutableStateOf(false)
    }
    val state = risingDoubleAsState(
        target = target.toDouble(),
        onComplete = {finished = true}
    )
    return object: State<Long> {
        override val value: Long
            get() = if (finished) target else state.value.toLong()
    }
}

@Composable
fun TransientVisibility(key: Any?, delayTime: Long = 2000, fadeTime: Long = 1000, contentAlignment: Alignment = Alignment.Center, modifier: Modifier = Modifier,  composable: @Composable BoxScope.() -> Unit) {
    var visibility by remember {
        mutableStateOf(0f)
    }
    LaunchedEffect(key1 = key) {
        visibility = 1f
        delay(delayTime)
        val start = System.currentTimeMillis()
        while (visibility > 0.001f) {
            delay(20)
            visibility = (start + fadeTime - System.currentTimeMillis())/fadeTime.toFloat()
        }
        visibility = 0f;
    }
    Box(contentAlignment = contentAlignment, modifier = Modifier
        .alpha(visibility)
        .then(modifier)) {
        this.composable()
    }
}

@Composable
fun Drawer(bottomThreshold: Float = 0.2f, topThreshold: Float = 0.8f, handleHeight: Dp = 30.dp, content: @Composable BoxScope.() -> Unit) {
    var maxHeight by remember {
        mutableStateOf(0.dp)
    }
    var height by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val autoSpeed = 0.2.dp //dp per millisecond
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                maxHeight = density.run { it.height.toDp() } - handleHeight; height =
                minOf(height, maxHeight)
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(handleHeight)
                    .clip(RoundedCornerShape(topStart = handleHeight, topEnd = handleHeight))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .pointerInput(null) {
                        awaitPointerEventScope {
                            var dragging = false
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Press) dragging = true
                                else if (event.type == PointerEventType.Release) {
                                    dragging = false
                                    val pos = height.value / maxHeight.value
                                    if (pos > topThreshold || pos < bottomThreshold)
                                        scope.launch {
                                            val start = System.currentTimeMillis()
                                            while (true) {
                                                val prog =
                                                    (autoSpeed.value * (System.currentTimeMillis() - start)).dp
                                                height += if (pos > topThreshold) prog else -prog
                                                if (height > maxHeight) {
                                                    height = maxHeight
                                                    break
                                                }
                                                if (height < 0.dp) {
                                                    height = 0.dp
                                                    break
                                                }
                                                delay(20)
                                            }
                                        }
                                } else if (event.type == PointerEventType.Move && dragging) {
                                    height -= density.run { event.calculatePan().y.toDp() }
                                    height = minOf(height, maxHeight)
                                }
                            }
                        }
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()

                        }
                    }
            ) {
                HorizontalDivider(modifier = Modifier.width(50.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                content = content
            )
        }
    }
}

@Composable
fun ScrollDisappearingHeader(
    scrollState: ScrollState,
    maxHeight: Dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    var previous by rememberSaveable {
        mutableStateOf(scrollState.value)
    }
    var height by rememberSaveable {
        mutableStateOf(maxHeight.value)
    }
    var restore by rememberSaveable {
        mutableStateOf(false)
    }
    val cur = scrollState.value
    if (!restore) {
        if (cur > previous) {
            //scroll down
            height -= cur - previous
            previous = cur
        }
        else if (cur < previous && height > 0) {
            height += previous - cur
            height = min(height, maxHeight.value)
            previous = cur
        }
        else if (cur < previous - 10) {
            restore = true
        }
    }
    LaunchedEffect(key1 = restore) {
        if (restore) {
            height = 0f
            while (height < maxHeight.value) {
                height += 5
                delay(20)
            }
            height = maxHeight.value
            restore = false
        }
    }

    Row(
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        modifier = Modifier
            .requiredHeightIn(max = height.dp)
            .clipToBounds()
            .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
            .then(modifier)
    ) {
        content()
    }
}