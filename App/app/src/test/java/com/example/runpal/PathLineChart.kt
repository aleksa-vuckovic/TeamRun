package com.example.runpal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.runpal.models.PathPoint
import com.example.runpal.ui.theme.MediumBlue
import com.example.runpal.ui.theme.Pink

val time = System.currentTimeMillis()
val inc = 10000L

val path = listOf(  PathPoint(altitude = 100.0, time = time, speed =  5.0, distance = 0.0, kcal = 0.0, end = false),
    PathPoint(altitude = 101.0, time = time + inc , speed =  5.0, distance = 0.0, kcal = 0.0, end = false),
    PathPoint(altitude = 102.0, time = time + 2*inc, speed =  5.0, distance = 0.0, kcal = 0.0, end = false),
    PathPoint(altitude = 103.0, time = time + 3*inc, speed =  5.0, distance = 0.0, kcal = 0.0, end = false),
    PathPoint(altitude = 105.0, time = time + 4*inc, speed =  5.0, distance = 0.0, kcal = 0.0, end = true),
    PathPoint(altitude = 105.0, time = time + 5*inc, speed =  5.0, distance = 0.0, kcal = 0.0, end = false),
    PathPoint(altitude = 105.0, time = time + 6*inc, speed =  5.0, distance = 0.0, kcal = 0.0, end = false),
    PathPoint(altitude = 104.0, time = time + 7*inc, speed =  5.0, distance = 0.0, kcal = 0.0, end = false),
    PathPoint(altitude = 103.0, time = time + 8*inc, speed =  5.0, distance = 0.0, kcal = 0.0, end = true)
)

val path2 = List<PathPoint>(100) {
    PathPoint(altitude = 100.0 + Math.sin(it.toDouble()/10)*5, time = time + it*inc, end = false)
}
val path3 = List<PathPoint>(100) {
    PathPoint(altitude = 100.0 + Math.sin(it.toDouble()/10 + 1)*5, time = time + it*inc, end = false)
}

@Composable
fun PathChartTest() {
    val datasets = remember {
        listOf(
            PathChartDataset(
                data = path2,
                xValue = {it.time.toDouble()},
                yValue = {it.altitude}
            ),
            PathChartDataset(
                data = path3,
                xValue = {it.time.toDouble()},
                yValue = {it.altitude}
            ),
        )
    }
    val labelStyle = MaterialTheme.typography.labelSmall
    val options = remember {
        listOf(
            ChartOptions(
                color = MediumBlue,
                shade = true,
                width = 15f,
                markers = true,
                markerLabel = AltitudeFormatter,
                markerLabelStyle = labelStyle
            ),
            ChartOptions(
                color = Pink,
                shade = true,
                width = 10f,
                markers = false,
                markerLabel = null
            )
        )
    }
    val axes = remember {
        AxesOptions(
            yLabel = AltitudeFormatter,
            labelStyle = labelStyle,
            xTickCount = 10,
            yTickCount = 5
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Chart(
            datasets = datasets,
            options = options,
            axesOptions = axes,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )
    }
}