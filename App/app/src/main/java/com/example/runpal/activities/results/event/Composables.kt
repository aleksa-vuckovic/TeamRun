package com.example.runpal.activities.results.event

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.runpal.R
import com.example.runpal.TimeFormatter
import com.example.runpal.activities.running.PanelText
import com.example.runpal.borderBottom
import com.example.runpal.models.Event
import com.example.runpal.models.EventResult
import com.example.runpal.ui.theme.Bronze
import com.example.runpal.ui.theme.DarkBronze
import com.example.runpal.ui.theme.DarkGold
import com.example.runpal.ui.theme.DarkSilver
import com.example.runpal.ui.theme.Gold
import com.example.runpal.ui.theme.LightBlue
import com.example.runpal.ui.theme.MainBlue
import com.example.runpal.ui.theme.RichBlack
import com.example.runpal.ui.theme.Silver

@Composable
fun EventResultScreen(ranking: List<EventResult>, user: String, event: Event) {
    val place = remember(ranking) {
        ranking.indexOfFirst { it.user == user }
    }
    val dq = place == -1 || ranking[place].disqualified == true
    val colors = remember(place) {
        if (place == 0 && !dq) Gold to DarkGold
        else if (place == 1 && !dq) Silver to DarkSilver
        else if (place == 2 && !dq) Bronze to DarkBronze
        else MainBlue to RichBlack
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .borderBottom()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = event.name, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .borderBottom()
            .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            val placetext = if (place == -1 || ranking[place].disqualified == true) "DQ" else
                ((place+1).toString() + (if (place == 0) stringResource(id = R.string.st) else if (place == 1) stringResource(id = R.string.nd) else if (place == 2) stringResource(id = R.string.rd) else stringResource(id = R.string.th)))
            val belowtext = if (placetext == "DQ") stringResource(id = R.string.better_luck_next_time)
                else stringResource(id = R.string.congrats_you_won)+ " ${placetext} " + stringResource(id = R.string.place) + "."
            Box(modifier = Modifier
                .width(150.dp)
                .height(200.dp)
                .shadow(elevation = 5.dp, shape = RoundedCornerShape(50.dp))
                .clip(shape = RoundedCornerShape(10.dp))
                .background(color = colors.first),
                contentAlignment = Alignment.Center
            ) {
                Text(text = placetext, style = MaterialTheme.typography.displayLarge, color = colors.second)
            }
            Text(text = belowtext)
        }
        Row(
            modifier = Modifier
                .borderBottom()
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(30.dp))
            Text(text = stringResource(id = R.string.user), style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = stringResource(id = R.string.time), style = MaterialTheme.typography.titleSmall)
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(ranking.indices.toList()) {
                val dq = ranking[it].disqualified == true
                Row(
                    modifier = Modifier
                        .borderBottom()
                        .background(
                            color =
                            if (it == 0 && !dq) Gold
                            else if (it == 1 && !dq) Silver
                            else if (it == 2 && !dq) Bronze
                            else if (it == place) MainBlue
                            else if (dq) Color.Gray
                            else Color.Transparent
                        )
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(text = "${if (dq) '-' else it+1}.", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(30.dp))
                    Text(text = "${ranking[it].name} ${ranking[it].last}", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.weight(1f))
                    Log.d("TEST", "TIME = ${ranking[it].time} and formatted ${TimeFormatter.format(ranking[it].time!!)}}")
                    PanelText(text = if (dq) "DQ" to "" else TimeFormatter.format(ranking[it].time!!))
                }
            }
        }
    }
}