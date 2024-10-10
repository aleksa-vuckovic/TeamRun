package com.example.runpal.activities.results.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.runpal.KcalFormatter
import com.example.runpal.Units
import com.example.runpal.activities.running.PanelText
import com.example.runpal.activities.running.RunState
import com.example.runpal.borderBottom
import com.example.runpal.borderRight
import com.example.runpal.models.Run
import com.example.runpal.models.RunData
import com.example.runpal.models.User
import com.example.runpal.ui.theme.LightGreen
import com.example.runpal.ui.theme.LightRed


@OptIn(ExperimentalCoilApi::class)
@Composable
fun UserRankingRow(value: Pair<String, String>,
                   user: User,
                   rank: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .borderBottom()
            .padding(10.dp)
        , verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(text = "${rank}.", style = MaterialTheme.typography.titleSmall, modifier = Modifier.width(30.dp))
        Image(painter = rememberImagePainter(data = user.profileUri),
            contentDescription = user.name,
            modifier = Modifier
                .size(50.dp)
                .clip(shape = RoundedCornerShape(5.dp)),
            contentScale = ContentScale.Crop)
        Text(text = user.name, style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.weight(1f))
        PanelText(text = value, modifier = Modifier
                .padding(10.dp)
        )
    }
}