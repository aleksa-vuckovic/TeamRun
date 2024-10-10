package com.example.runpal.activities.results.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AreaChart
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.runpal.Destination
import com.example.runpal.R


object ChartsDestination: Destination {
    override val argsRoute: String = "charts"
    override val baseRoute: String = "charts"
    override val icon: ImageVector = Icons.Default.AreaChart
    override val label: Int = R.string.details
    override val title: Int = R.string.details
}

object ResultsDestination: Destination {
    override val argsRoute: String = "results"
    override val baseRoute: String = "results"
    override val icon: ImageVector = Icons.AutoMirrored.Filled.DirectionsRun
    override val label: Int = R.string.results
    override val title: Int = R.string.results
}

object RankingDestination: Destination {
    override val argsRoute: String = "ranking"
    override val baseRoute: String = "ranking"
    override val icon: ImageVector = Icons.Default.SportsScore
    override val label: Int = R.string.ranking
    override val title: Int = R.string.ranking

}

val bottomBarDestinations = listOf(ChartsDestination, ResultsDestination, RankingDestination)
val destinationsMap = mapOf(
    ChartsDestination.argsRoute to ChartsDestination,
    ResultsDestination.argsRoute to ResultsDestination,
    RankingDestination.argsRoute to RankingDestination
)