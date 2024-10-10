package com.example.runpal.activities.running.group

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.runpal.DEFAULT_PROFILE_URI
import com.example.runpal.KcalFormatter
import com.example.runpal.LoadingDots
import com.example.runpal.MetricDistanceFormatter
import com.example.runpal.R
import com.example.runpal.Units
import com.example.runpal.activities.running.PanelText
import com.example.runpal.activities.running.RunState
import com.example.runpal.borderBottom
import com.example.runpal.borderRight
import com.example.runpal.models.PathPoint
import com.example.runpal.models.Room
import com.example.runpal.models.Run
import com.example.runpal.models.User
import com.example.runpal.ui.theme.BadgeType
import com.example.runpal.ui.theme.LightGreen
import com.example.runpal.ui.theme.LightRed
import com.example.runpal.ui.theme.StandardBadge
import com.example.runpal.ui.theme.StandardButton
import com.example.runpal.ui.theme.StandardOutlinedTextField
import com.example.runpal.ui.theme.TransparentWhite

@Composable
fun EntryScreen( onJoin: (String) -> Unit, onCreate: () -> Unit, modifier: Modifier = Modifier) {

    var room by rememberSaveable {
        mutableStateOf("")
    }

    val round = 20.dp
    val style = MaterialTheme.typography.displaySmall

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {

        Text(text = stringResource(id = R.string.paste_room_here),
            style = style,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp)
        ) {
            StandardOutlinedTextField(value = room,
                onChange = {room = it},
                shape = RoundedCornerShape(topStart = round, topEnd = round, bottomStart = 0.dp, bottomEnd = 0.dp),
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth())
            Button(onClick = { onJoin(room) },
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = round, bottomEnd = round),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightGreen,
                    contentColor = Color.Black
                )
            ) {
                Text(text = stringResource(id = R.string.join))
            }
        }

        Text(text = stringResource(id = R.string.or_create_and_share),
            style = style,
            textAlign = TextAlign.Center)

        StandardButton(onClick = onCreate) {
            Text(text = stringResource(id = R.string.create))
        }
    }
}


@OptIn(ExperimentalCoilApi::class)
@Composable
fun LobbyScreen(room: Room,
                users: Map<String, User>,
                state: LobbyViewModel.State,
                onCopy: () -> Unit,
                onLeave: () -> Unit,
                onReady: () -> Unit,
                modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {

            Text(text = stringResource(id = R.string.copy_and_share_code))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(start = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = room._id, modifier = Modifier.weight(0.8f))
                IconButton(onClick = onCopy, modifier = Modifier.weight(0.2f)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy room code.")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = stringResource(id = R.string.members) + ": ${room.members.size}/5")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                for (member in room.members) {
                    val user = users[member]
                    if (user == null) continue
                    val ready: Boolean = room.ready.contains(member)
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                            .shadow(elevation = 10.dp)
                            .background(color = if (ready) LightGreen else MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.8f)
                                .padding(10.dp)
                        ) {
                            Image(
                                painter = rememberImagePainter(data = user.profileUri),
                                contentDescription = "Profile picture of ${user.name} ${user.last}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(10.dp))
                                    .size(100.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${user.name} ${user.last}",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.2f)
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (ready) Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = stringResource(id = R.string.ready),
                                modifier = Modifier.size(50.dp)
                            )
                        }

                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (state == LobbyViewModel.State.LOADING) LoadingDots(size = 18.dp, count = 3)
            else if (state == LobbyViewModel.State.WAITING) {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .weight(0.5f)
                    .clickable { onLeave() }
                    .background(color = LightRed),
                    contentAlignment = Alignment.Center) {
                    Text(text = stringResource(id = R.string.leave), style = MaterialTheme.typography.labelMedium)
                }
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .weight(0.5f)
                    .clickable { onReady() }
                    .background(color = LightGreen),
                    contentAlignment = Alignment.Center) {
                    Text(text = stringResource(id = R.string.im_ready), style = MaterialTheme.typography.labelMedium)
                }
            }
            else if (state == LobbyViewModel.State.READY) Text(text = stringResource(id = R.string.waiting_for_others))
        }
    }
}

@Composable
fun MapRanking(runStates: List<RunState>, users: List<User>, units: Units, pace: Boolean) {
    var show by rememberSaveable {
        mutableStateOf(false)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .padding(top = 20.dp)
            .size(70.dp)
            .align(Alignment.TopEnd)
            .clip(
                shape = RoundedCornerShape(
                    topStart = 10.dp,
                    bottomStart = 10.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                )
            )
            .clickable { show = !show }
            .background(color = TransparentWhite),
            contentAlignment = Alignment.Center
            ) {
            Icon(painter = painterResource(id = R.drawable.podium),
                contentDescription = "Live ranking",
                modifier = Modifier.fillMaxSize(0.8f))
        }
        AnimatedVisibility(visible = show,
            enter = slideInVertically(
                animationSpec = tween(
                    durationMillis = 800,
                    easing = LinearOutSlowInEasing,
                ), initialOffsetY = {it}
            ), exit = slideOutVertically(
                animationSpec = tween(
                    durationMillis = 800,
                    easing = LinearOutSlowInEasing
                ), targetOffsetY = {it}
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
        ) {
            val data = runStates.zip(users).sortedBy { -it.first.location.distance }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 5.dp, shape = RoundedCornerShape(topEnd = 10.dp))
                    .clip(shape = RoundedCornerShape(topEnd = 10.dp))
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                for (i in 1..data.size) {
                    UserRankingRow(runState = data[i-1].first, user = data[i - 1].second, rank = i, units = units, pace = pace)
                }
            }


        }
    }
}



@OptIn(ExperimentalCoilApi::class)
@Composable
fun UserRankingRow(runState: RunState,
                   user: User,
                   rank: Int,
                   units: Units,
                   pace: Boolean) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .borderBottom()
            .padding(10.dp)
        , verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(text = "${rank}.", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(15.dp))
        Image(painter = rememberImagePainter(data = user.profileUri),
            contentDescription = user.name,
            modifier = Modifier
                .size(50.dp)
                .clip(shape = RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Crop)
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = user.name, style = MaterialTheme.typography.labelMedium)
            val badgeSize = 8.sp;
            if (runState.run.state == Run.State.ENDED) StandardBadge(text = stringResource(id = R.string.finish_lower), type = BadgeType.DANGER, fontSize = badgeSize, padding = 1.dp)
            else if (runState.run.state == Run.State.PAUSED) StandardBadge(text = stringResource(id = R.string.pause_lower), type = BadgeType.INFO, fontSize = badgeSize, padding = 1.dp)
            else if (runState.run.state == Run.State.READY) StandardBadge(text = stringResource(id = R.string.start_lower), type = BadgeType.SUCCESS, fontSize = badgeSize, padding = 1.dp)
        }

        val big = MaterialTheme.typography.labelMedium
        val small = MaterialTheme.typography.labelSmall.copy(fontSize = 5.sp)
        Spacer(modifier = Modifier.weight(1f))
        PanelText(text = if (pace) units.pace.formatter.format(runState.location.speed)
                        else units.speed.formatter.format(runState.location.speed),
            modifier = Modifier
                .borderRight()
                .padding(5.dp),
            bigStyle = big,
            smallStyle = small
        )
        PanelText(text = units.distance.formatter.format(runState.location.distance),
            modifier = Modifier
                .borderRight()
                .padding(5.dp),
            bigStyle = big,
            smallStyle = small
        )
        PanelText(text = KcalFormatter.format(runState.location.kcal),
            modifier = Modifier
                .padding(5.dp),
            bigStyle = big,
            smallStyle = small
        )
    }
}

val fakeUser = User(name = "Aleksa Vuckovic", last = "Last", profile = DEFAULT_PROFILE_URI.toString())
val fakeRunState = object: RunState {
    override val run: Run = Run()
    override val location: PathPoint = PathPoint(
        distance = 100.0,
        kcal = 120.0,
        speed = 3.0
    )
    override val path: List<PathPoint> = listOf()
}
val fakeUser2 = User(name = "Name2", last = "Last2", profile = DEFAULT_PROFILE_URI.toString())
val fakeRunState2 = object: RunState {
    override val run: Run = Run()
    override val location: PathPoint = PathPoint(
        distance = 90.0,
        kcal = 110.0
    )
    override val path: List<PathPoint> = listOf()
}

@Preview
@Composable
fun Preview() {
    Box(modifier = Modifier.size(500.dp)) {
        //MapRanking(runStates = listOf(), users = listOf(), units = Units.METRIC, pace = false)
        UserRankingRow(runState = fakeRunState, user = fakeUser, rank = 1, units = Units.METRIC, pace = true)
    }
}