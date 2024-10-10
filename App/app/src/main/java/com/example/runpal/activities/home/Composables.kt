package com.example.runpal.activities.home

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimeInput
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.asDoubleState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.runpal.DateOnlyFormatter
import com.example.runpal.DoubleInput
import com.example.runpal.UTCDateTimeFormatter
import com.example.runpal.ImageSelector
import com.example.runpal.LoadingScreen
import com.example.runpal.LongTimeFormatter
import com.example.runpal.R
import com.example.runpal.TimeFormatter
import com.example.runpal.Units
import com.example.runpal.activities.running.PanelText
import com.example.runpal.risingDoubleAsState
import com.example.runpal.borderBottom
import com.example.runpal.join
import com.example.runpal.limitText
import com.example.runpal.models.Event
import com.example.runpal.models.PathPoint
import com.example.runpal.models.Run
import com.example.runpal.models.RunData
import com.example.runpal.risingLongAsState
import com.example.runpal.AxesOptions
import com.example.runpal.BitmapDescriptorStore
import com.example.runpal.CLASS_NAME_KEY
import com.example.runpal.CenteredMarker
import com.example.runpal.Chart
import com.example.runpal.ChartDataset
import com.example.runpal.ChartOptions
import com.example.runpal.ChartType
import com.example.runpal.DoubleInputWithUnit
import com.example.runpal.Drawer
import com.example.runpal.GoogleMapPath
import com.example.runpal.LocationService
import com.example.runpal.MAP_BUTTON_ALPHA
import com.example.runpal.MAP_BUTTON_COLOR
import com.example.runpal.MAP_ID
import com.example.runpal.PACKAGE_KEY
import com.example.runpal.RUN_MARKER_COLORS
import com.example.runpal.ScrollDisappearingHeader
import com.example.runpal.TransientVisibility
import com.example.runpal.activities.results.FittedGoogleMap
import com.example.runpal.activities.running.GoogleMapEventPath
import com.example.runpal.activities.running.MapState
import com.example.runpal.animateDpAsStateCustom
import com.example.runpal.darker
import com.example.runpal.hasLocationPermission
import com.example.runpal.lighter
import com.example.runpal.models.distance
import com.example.runpal.models.toDistance
import com.example.runpal.models.toLatLng
import com.example.runpal.models.toPathPoint
import com.example.runpal.ripple
import com.example.runpal.ui.theme.BadgeType
import com.example.runpal.ui.theme.LightGreen
import com.example.runpal.ui.theme.MediumBlue
import com.example.runpal.ui.theme.StandardBadge
import com.example.runpal.ui.theme.StandardButton
import com.example.runpal.ui.theme.StandardSpinner
import com.example.runpal.ui.theme.StandardStatRow
import com.example.runpal.ui.theme.StandardTextField
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.ktx.buildGoogleMapOptions
import kotlinx.coroutines.delay
import java.lang.Math.signum
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun HomeButton(icon: ImageVector, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(start = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text,
            modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text,
            //.wrapContentHeight(align = Alignment.CenterVertically),
            //.clickable { onClick() },
            style = MaterialTheme.typography.displayMedium)
            //textAlign = TextAlign.Center)
    }

}

@Composable
fun MenuScreen(onSoloRun: () -> Unit, onGroupRun: () -> Unit, onEvent: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        HomeButton(
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            text = stringResource(id = R.string.solo_run),
            onClick = onSoloRun,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        HomeButton(
            icon = Icons.Default.Groups,
            text = stringResource(id = R.string.group_run),
            onClick = onGroupRun,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        HomeButton(
            icon = Icons.Default.SportsScore,
            text = stringResource(id = R.string.event),
            onClick = onEvent,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun SmallEventCard(event: Event, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick() }
            .width(150.dp)
            .fillMaxHeight()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val textColor = MaterialTheme.colorScheme.onPrimaryContainer
        Image(painter = rememberImagePainter(data = event.imageUri),
            contentDescription = event.name,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .size(130.dp)
                .clip(shape = RoundedCornerShape(15.dp))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = event.name,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = textColor)
        Spacer(modifier = Modifier.height(10.dp))
        EventStatus(event = event, textColor = textColor)

    }
}

@Composable
fun EventsRow(events: List<Event>, onClick: (Event) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Text(text = stringResource(id = R.string.events_you_follow),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(10.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
                .height(250.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (event in events) {
                SmallEventCard(event = event, onClick = {onClick(event)})
            }
            if (events.size == 0) Text(text = stringResource(id = R.string.no_events_to_show),
                modifier = Modifier.padding(10.dp))
        }
    }
}

@Composable
fun BigEventCard(event: Event, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        val textColor = MaterialTheme.colorScheme.onSurface
        Image(painter = rememberImagePainter(data = event.imageUri),
            contentDescription = event.name,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier
                .size(130.dp)
                .clip(shape = RoundedCornerShape(15.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = event.name,
                style = MaterialTheme.typography.titleSmall,
                color = textColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = limitText(event.description),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(text = UTCDateTimeFormatter.format(event.time).first,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
                )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventsScreen(followedEvents: List<Event>,
                 searchEvents: List<Event>,
                 onClick: (Event) -> Unit,
                 onSearch: (String) -> Unit,
                 onCreate: () -> Unit,
                 modifier: Modifier = Modifier) {
    var search by rememberSaveable {
        mutableStateOf("")
    }
    var searching by rememberSaveable {
        mutableStateOf(false)
    }
    val limit = if (searching) 0.dp else 350.dp
    val height = animateDpAsStateCustom(target = limit, duration = 1000L)
    val keyboard = LocalSoftwareKeyboardController.current
    BackHandler(searching) {
        searching = false
        search = ""
        onSearch("")
        keyboard?.hide()
    }
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeightIn(max = height.value)
                    .clipToBounds()
                    .wrapContentHeight(align = Alignment.Top, unbounded = true)
            ) {
                EventsRow(
                    events = followedEvents,
                    onClick = onClick,
                    modifier = Modifier
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreate() }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = stringResource(id = R.string.create_event))
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = stringResource(id = R.string.create_event)
                    )
                }
            }
        }
        stickyHeader {
            TextField(
                value = search,
                onValueChange = { search = it; onSearch(it); searching = true; },
                trailingIcon = {
                    Icon(
                        imageVector = if (searching) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = stringResource(id = R.string.search),
                        modifier = if (searching) Modifier.clickable {
                            searching = false
                            search = ""
                            onSearch("")
                            keyboard?.hide()
                        } else Modifier
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(id = R.string.search_upcoming_events))
                }
            )
        }
        items(searchEvents) {
            BigEventCard(
                event = it,
                onClick = { onClick(it) },
                modifier = Modifier
                    .padding(10.dp)
                    .borderBottom(strokeWidth = 1.dp, color = Color.Black)
            )
        }
        if (searchEvents.size == 0) {
            item {Text(text = stringResource(id = R.string.no_matching_events), modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp), textAlign = TextAlign.Center)}
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(onCreate: (String, String, Long?, Double, Uri?, path: List<LatLng>?, tolerance: Double?) -> Unit,
                      errorMessage: String?,
                      preferredUnits: Units,
                      modifier: Modifier = Modifier) {

    val context = LocalContext.current
    LaunchedEffect(key1 = errorMessage) {
        if (!errorMessage.isNullOrEmpty())
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
    var name by rememberSaveable {
        mutableStateOf("")
    }
    var description by rememberSaveable {
        mutableStateOf("")
    }
    var distance by rememberSaveable {
        mutableStateOf(0.0)
    }
    var units by remember {
        mutableStateOf(preferredUnits)
    }
    val time = rememberTimePickerState(initialHour = 12, initialMinute = 0, is24Hour = true)
    var timeDialog by rememberSaveable {
        mutableStateOf(false)
    }
    val date = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var dateDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var image by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }
    var path by rememberSaveable {
        mutableStateOf(listOf<LatLng>())
    }
    var tolerance by rememberSaveable {
        mutableStateOf(10.0)
    }
    var toleranceInput by rememberSaveable {
        mutableStateOf(0f)
    }
    val mapState = remember {
        MapState(false)
    }

    fun selectedTime() = if (date.selectedDateMillis == null) null else date.selectedDateMillis!! + (time.hour*60+time.minute)*60000

    val navController = rememberNavController()
    val infoStep = "info"
    val pathStep = "path"
    val titleStyle = MaterialTheme.typography.titleSmall

    var position: LatLng? by remember {
        mutableStateOf(null)
    }

    @Composable fun Container(content: @Composable ColumnScope.() -> Unit) = Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
    @Composable fun Navigation(back: Boolean = true, next: String? = null, create: (() -> Unit)? = null) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            StandardButton(onClick = { navController.popBackStack() }, enabled = back) {
                Text("Back")
            }
            if (next != null) StandardButton(onClick = { navController.navigate(next) })
            {
                Text("Next")
            }
            if (create != null) StandardButton(onClick = create) {
                Text(stringResource(id = R.string.create))
            }
        }
    }
    @Composable
    fun RowScope.Label(text: String) = Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .padding(end = 30.dp)
            .fillMaxWidth(0.3f),
        textAlign = TextAlign.End
    )

    DisposableEffect(null) {
        val connection = object: ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                (p1 as LocationService.LocalBinder).setListener { position = it.toLatLng() }
            }
            override fun onServiceDisconnected(p0: ComponentName?) {}
        }
        if (!context.hasLocationPermission())
            onDispose{}
        else {
            val intent = Intent(context, LocationService::class.java)
            context.startService(intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            onDispose { context.unbindService(connection) }
        }
    }

    NavHost(
        navController = navController,
        startDestination = infoStep,
        modifier = modifier
    ) {
        composable(route = infoStep) {
            Container {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Label(stringResource(id = R.string.event_name))
                    StandardTextField(value = name, onChange = {name = it}, modifier = Modifier.width(200.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Label(stringResource(id = R.string.event_image))
                    ImageSelector(input = image, onSelect = { image = it }, Modifier.size(200.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Label(stringResource(id = R.string.date_and_time_utc))
                    StandardTextField(
                        value = if (selectedTime() != null) UTCDateTimeFormatter.format(selectedTime()!!).first else "Click to select",
                        onChange = {},
                        enabled = false,
                        modifier = Modifier
                            .clickable { dateDialog = true }
                            .width(200.dp))
                }
                if (dateDialog) DatePickerDialog(
                    onDismissRequest = { dateDialog = false },
                    confirmButton = {
                        Button(onClick = { dateDialog = false; timeDialog = true }) {
                            Text(stringResource(id = R.string.next))
                        }
                    }) {
                    DatePicker(state = date)
                }
                if (timeDialog) DatePickerDialog(
                    onDismissRequest = { timeDialog = false },
                    confirmButton = {
                        Button(onClick = { timeDialog = false }) {
                            Text(stringResource(id = R.string.ok))
                        }
                    },
                    modifier = Modifier.padding(50.dp)
                ) {
                    TimeInput(
                        state = time, modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Label(stringResource(id = R.string.description))
                    StandardTextField(value = description,
                        onChange = {description = it},
                        minLines = 4,
                        modifier = Modifier.width(250.dp))
                }
                Navigation(back = false, next = pathStep)
            }
        }

        composable(route = pathStep) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Container {
                    Text(text = stringResource(id = R.string.event_route), style = titleStyle)
                    Text(
                        text = stringResource(id = R.string.if_you_define_a_route),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surfaceVariant.darker(0.5f),
                                shape = RoundedCornerShape(10)
                            )
                            .clip(shape = RoundedCornerShape(10))
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Label(stringResource(id = R.string.distance))
                        DoubleInputWithUnit(
                            value = units.distance.scale(Units.METRIC.distance.convert(distance, units), to = units.distance.highrange),
                            onChange = { if (path.isEmpty()) distance = units.distance.convert(units.distance.scale(it, from=units.distance.highrange), Units.METRIC)},
                            unit = units.distance.highrange,
                            onChangeUnit = { units = units.next },
                            enabled = path.isEmpty(),
                            modifier = Modifier
                                .height(60.dp)
                                .width(200.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(R.string.fault_tolerance), style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        Slider(
                            enabled = path.isNotEmpty(),
                            valueRange = 0f..100f,
                            value = toleranceInput,
                            onValueChange = {
                                toleranceInput = it
                                tolerance = 2.0/4375*it*it*it + 3.0/875*it*it - 1.0/70*it + 10.0
                            }
                        )
                        if (path.isNotEmpty()) Text(text = units.distance.formatter.format(tolerance.toDouble()).join(""))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Navigation(create = { onCreate(name, description, selectedTime(), distance, image, path, tolerance) })
                }

                Drawer(bottomThreshold = 0.4f, topThreshold = 0.4f) {
                    EventPathInputMap(
                        mapState = mapState,
                        path = path,
                        onCenterSwitch = {
                            if (position != null) mapState.cameraPositionState.position =
                                CameraPosition.fromLatLngZoom(
                                    position!!,
                                    mapState.cameraPositionState.position.zoom
                                )
                        },
                        onAddPoint = {
                            path += it
                            distance = path.toDistance()
                        },
                        onUndo = {
                            if (!path.isEmpty())
                                path -= path.last()
                            distance = path.toDistance()
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(1))
                    )
                }
            }
        }
    }
}

@Composable
fun EventStatus(event: Event, textColor: Color) {
    if (event.status == Event.Status.UPCOMING)
        Text(text = stringResource(id = R.string.starting_in) + " " + LongTimeFormatter.format(event.time - System.currentTimeMillis()).first,
            style = MaterialTheme.typography.bodySmall,
            color = textColor)
    else if (event.status == Event.Status.CURRENT)
        StandardBadge(text = stringResource(id = R.string.happening_now),
            type = BadgeType.SUCCESS
        )
    else StandardBadge(text = stringResource(id = R.string.past),
        type = BadgeType.DANGER
    )
}

@Composable
fun EventScreen(event: Event, onJoin: () -> Unit, onFollow: () -> Unit, onUnfollow: () -> Unit, units: Units = Units.METRIC, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                val textColor = MaterialTheme.colorScheme.onPrimaryContainer
                Image(
                    painter = rememberImagePainter(data = event.imageUri),
                    contentDescription = event.name,
                    modifier = Modifier
                        .sizeIn(140.dp, 140.dp, 160.dp, 160.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.borderBottom(strokeWidth = 1.dp)
                    )
                    Text(
                        text = UTCDateTimeFormatter.format(event.time).first,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StandardBadge(text = event.followers.toString(), fontSize = 20.sp)
                        Text(
                            text = stringResource(id = R.string.of_followes),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    EventStatus(event = event, textColor = textColor)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = event.description,
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${stringResource(R.string.distance)}: ${units.distance.formatter.format(event.distance).join()}",
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (event.status == Event.Status.CURRENT)
                        ElevatedButton(
                            onClick = onJoin,
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = LightGreen
                            )
                        ) {
                            Text(text = stringResource(id = R.string.join_now))
                        }
                    if (event.following)
                        ElevatedButton(
                            onClick = onUnfollow,
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(text = stringResource(id = R.string.unfollow))
                        }
                    else ElevatedButton(
                        onClick = onFollow,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(text = stringResource(id = R.string.follow))
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
        
        if (event.path != null) Drawer {
            val mapMin = remember(event) {
                LatLng(event.path.minOf { it.latitude }, event.path.minOf { it.longitude })
            }
            val mapMax = remember(event) {
                LatLng(event.path.maxOf { it.latitude }, event.path.maxOf { it.longitude })
            }
            FittedGoogleMap(
                mapMin = mapMin,
                mapMax = mapMax,
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                GoogleMapEventPath(path = event.path, complete = true)
            }
        }
    }
}


@Composable
fun RunInfo(runData: RunData, onClick: () -> Unit, units: Units, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(10.dp))
            .clip(shape = RoundedCornerShape(10.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = DateOnlyFormatter.format(runData.run.start!!).first, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.width(10.dp))
        if (runData.run.room != null) StandardBadge(text = stringResource(id = R.string.group), type = BadgeType.INFO)
        else if (runData.run.event != null) StandardBadge(text = stringResource(id = R.string.event_lowercase), type = BadgeType.SUCCESS)
        Spacer(modifier = Modifier.weight(1f))
        PanelText(text = TimeFormatter.format(runData.run.running), modifier = Modifier
            .padding(10.dp))
        PanelText(text = units.distance.formatter.format(runData.location.distance), modifier = Modifier
            .padding(10.dp))
    }
}

@Composable
fun HistoryScreen(onClick: (Run) -> Unit, units: Units) {
    val vm: HistoryViewModel = hiltViewModel()
    LazyColumn(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .padding(start = 20.dp, end = 20.dp),
        state = rememberLazyListState()
    ) {
        items(vm.runs) {
            RunInfo(runData = it, onClick = { onClick(it.run) }, units = units, modifier = Modifier.padding(20.dp))
        }
        if (!vm.end) item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(50.dp))
                LaunchedEffect(key1 = vm.runs) {
                    vm.more()
                }
            }
        }
        if (vm.runs.isEmpty()) item {
            Text(text = stringResource(id = R.string.your_runs_will_appear_here))
        }
    }
}

@Composable
fun StatsScreen(units: Units) {
    val vm: StatsViewModel = hiltViewModel()

    if (vm.state == StatsViewModel.State.LOADING) LoadingScreen()
    else {
        var selected by rememberSaveable {
            mutableStateOf(vm.options.last())
        }
        val totalKm by risingDoubleAsState(target = vm.totalKm[selected]!!)
        val totalTime by risingLongAsState(target = vm.totalTime[selected]!!)
        val dataset = ChartDataset(data = vm.runData[selected]!!, xValue={it.run.start!!.toDouble()}, yValue={it.location.distance})
        val options = ChartOptions(
            color = MediumBlue,
            width = vm.chartWidthMap[selected]!!,
            markers = true,
            markerLabel = units.distance.formatter,
            show = true,
            type = ChartType.SCATTER
        )
        val axesOptions = AxesOptions(
            labelStyle = MaterialTheme.typography.labelSmall,
            yLabel = units.distance.formatter,
            yTickCount = 5,
            xExpandFactor = 1.2,
            yExpandFactor = 1.4,
            xSpanMin = 24*60*60*1000.0
        )
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val scrollState = rememberScrollState()
            ScrollDisappearingHeader(
                scrollState = scrollState,
                maxHeight = 75.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                StandardSpinner(
                    values = vm.options,
                    selected = selected,
                    onSelect = {selected = it}
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = stringResource(id = R.string.total_distance),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                PanelText(
                    text = units.distance.formatter.format(totalKm),
                    bigStyle = MaterialTheme.typography.displayLarge,
                    smallStyle = MaterialTheme.typography.displaySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .borderBottom()
                        .padding(10.dp)
                )
                StandardStatRow(
                    name = stringResource(id = R.string.best_run),
                    value = units.distance.formatter.format(vm.bestKm[selected]!!)
                )
                StandardStatRow(
                    name = stringResource(id = R.string.average_run),
                    value = units.distance.formatter.format(vm.avgKm[selected]!!)
                )

                Spacer(modifier = Modifier.height(30.dp))
                Chart(
                    datasets = listOf(dataset),
                    options = listOf(options),
                    axesOptions = axesOptions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                )
                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = stringResource(id = R.string.total_running_time),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                PanelText(
                    text = LongTimeFormatter.format(totalTime),
                    bigStyle = MaterialTheme.typography.displayMedium,
                    smallStyle = MaterialTheme.typography.displaySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .borderBottom()
                        .padding(10.dp)
                )
                StandardStatRow(
                    name = stringResource(id = R.string.longest_run),
                    value = TimeFormatter.format(vm.longestTime[selected]!!)
                )
            }
        }
    }
}

fun Offset.toIntOffset(): IntOffset = IntOffset(x.toInt(), y.toInt())
fun LatLng.toShortString() = String.format("%.4f | %.4f", latitude, longitude)
@Composable
fun EventPathInputMap(mapState: MapState,
                      path: List<LatLng>,
                      onCenterSwitch: () -> Unit,
                      onAddPoint: (LatLng) -> Unit,
                      onUndo: () -> Unit,
                      modifier: Modifier = Modifier) {
    var mapSize by remember {
        mutableStateOf(Size(0f,0f))
    }
    var auto by remember {
        mutableStateOf(false)
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { mapSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(auto) {
                    if (auto)
                        awaitPointerEventScope {
                            var prev: LatLng? = null;
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                if (event.type == PointerEventType.Move) {
                                    val target = mapState.cameraPositionState.position.target
                                    if (prev == null || target.distance(prev) > 50) {
                                        onAddPoint(target)
                                        prev = target
                                    }
                                }
                            }
                        }
                },
            cameraPositionState = mapState.cameraPositionState,
            googleMapOptionsFactory = { buildGoogleMapOptions { mapId(MAP_ID) } },
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                tiltGesturesEnabled = false,
                rotationGesturesEnabled = false
            )
        ) {
            GoogleMapEventPath(path = path, complete = true)
        }
        TransientVisibility(key = mapState.cameraPositionState.position.target, modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 10.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15))
            .background(Color.White.copy(alpha = 0.8f))
            .padding(start = 10.dp, end = 10.dp)) {
            Text(text = mapState.cameraPositionState.position.target.toShortString(), color = Color.Black)
        }

        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            VerticalDivider(
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxHeight()
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(modifier = Modifier
            .size(7.dp)
            .clip(RoundedCornerShape(50))
            .background(
                RUN_MARKER_COLORS[0]
            )
            .align(Alignment.Center))
        //Box containing buttons
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(9.dp)
        ) {
            //Center switch button
            IconButton(
                onClick = onCenterSwitch,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp)
                    .clip(CircleShape)
                    .background(color = MAP_BUTTON_COLOR)
                    //.ripple()
            ) {
                Icon(
                    imageVector = if (mapState.centered) Icons.Default.CropFree else Icons.Default.CenterFocusStrong,
                    contentDescription = if (mapState.centered) "Recenter" else "Decenter",
                    tint = Color.Black
                )
            }
            //Add point button
            IconButton(
                onClick = { onAddPoint(mapState.cameraPositionState.position.target)},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(75.dp)
                    .clip(CircleShape)
                    .background(color = MAP_BUTTON_COLOR)
                    .ripple(color = RUN_MARKER_COLORS[0].copy(alpha = MAP_BUTTON_ALPHA))
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Add current point to path",
                    tint = Color.Black
                )
            }
            Column(
                modifier = Modifier.align(Alignment.TopEnd),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                horizontalAlignment = Alignment.End
            ) {
                //Auto button
                Row(modifier = Modifier
                    .clip(RoundedCornerShape(20))
                    .background(color = MAP_BUTTON_COLOR)
                    .clickable { auto = !auto }
                    //.ripple()
                    .padding(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(text = "AUTO", fontWeight = FontWeight.Bold, color = Color.Black, style = MaterialTheme.typography.labelSmall)
                    StandardBadge(text = if (auto) "on" else "off", type = if (auto) BadgeType.SUCCESS else BadgeType.DANGER, fontSize = 8.sp)
                }
                //Undo button
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color = MAP_BUTTON_COLOR)
                        //.ripple()
                ) {
                    Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo", tint = Color.Black)
                }
            }

        }
    }
}

//////////////////////////Previews

@Preview
@Composable
fun PreviewHomeButton() {
    val runData = RunData(
        run = Run(start = System.currentTimeMillis(), running = 34 * 60000, event = ""),
        location = PathPoint(distance = 1256.0)
    )
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        RunInfo(
            runData = runData, onClick = { }, units = Units.METRIC,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        )
        RunInfo(
            runData = runData, onClick = { }, units = Units.METRIC,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        )
    }
}

