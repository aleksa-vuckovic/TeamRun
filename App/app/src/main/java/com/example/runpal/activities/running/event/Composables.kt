package com.example.runpal.activities.running.event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.runpal.R
import com.example.runpal.TimeFormatter
import com.example.runpal.Units
import com.example.runpal.activities.running.PanelText
import com.example.runpal.activities.running.RunState
import com.example.runpal.activities.running.group.UserRankingRow
import com.example.runpal.borderBottom
import com.example.runpal.models.EventResult
import com.example.runpal.models.User
import com.example.runpal.ui.theme.TransparentWhite

@Composable
fun MapRanking(ranking: List<EventResult>, units: Units) {
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 5.dp, shape = RoundedCornerShape(topEnd = 10.dp))
                    .clip(shape = RoundedCornerShape(topEnd = 10.dp))
                    .background(color = MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                for (i in ranking.indices) {
                    Row(
                        modifier = Modifier
                            .borderBottom()
                            .padding(10.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${i+1}.", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(30.dp))
                        Text(text = "${ranking[i].name} ${ranking[i].last}", style = MaterialTheme.typography.titleSmall)
                        if (ranking[i].finished) Icon(imageVector = Icons.Default.SportsScore, contentDescription = "Finished race")
                        Spacer(modifier = Modifier.weight(1f))
                        if (ranking[i].finished) PanelText(text = TimeFormatter.format(ranking[i].time!!))
                        else PanelText(text = units.distance.formatter.format(ranking[i].distance!!))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RankingPreview() {
    val ranks = listOf(
        EventResult(name = "Some", last = "Name", time = 12*60000),
        EventResult(name = "Some2", last = "Name", time = 13*60200),
        EventResult(name = "Some3", last = "Name", time = 14*60500),
        EventResult(name = "Some4", last = "Name", distance = 1289.0),
        EventResult(name = "Some5", last = "Name", distance = 1223.0)
    )
    Box(modifier = Modifier.size(500.dp)) {
        MapRanking(ranking = ranks, units = Units.METRIC)
    }
}