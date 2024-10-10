package com.example.runpal.activities.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.runpal.Destination
import com.example.runpal.EVENT_ID_KEY
import com.example.runpal.R


object HistoryDestination: Destination {
    override val argsRoute: String = "history"
    override val baseRoute: String = "history"
    override val icon: ImageVector = Icons.Default.History
    override val label: Int = R.string.history
    override val title: Int = R.string.history
}
object MenuDestination: Destination {
    override val argsRoute: String = "menu"
    override val baseRoute: String = "menu"
    override val icon: ImageVector = Icons.Default.Home
    override val label: Int = R.string.run
    override val title: Int = R.string.run
}
object StatsDestination: Destination {
    override val argsRoute: String = "stats"
    override val baseRoute: String = "stats"
    override val icon: ImageVector = Icons.Default.QueryStats
    override val label: Int = R.string.stats
    override val title: Int = R.string.stats
}

val navBarDestinations = listOf(HistoryDestination, MenuDestination, StatsDestination)
val destinationsMap = mapOf(
    HistoryDestination.argsRoute to HistoryDestination,
    MenuDestination.argsRoute to MenuDestination,
    StatsDestination.argsRoute to StatsDestination,
    EventsDestination.argsRoute to EventsDestination,
    EventDestination.argsRoute to EventDestination,
    CreateEventDestination.argsRoute to CreateEventDestination
)


object EventsDestination: Destination {
    override val argsRoute: String = "events"
    override val baseRoute: String = "events"
    override val icon: ImageVector? = null
    override val label: Int? = null
    override val title: Int = R.string.events
}

object EventDestination: Destination {
    override val argsRoute: String = "event/{${EVENT_ID_KEY}}"
    override val baseRoute: String = "event/"
    override val icon: ImageVector? = null
    override val label: Int? = null
    override val title: Int = R.string.event
    val arg: String = EVENT_ID_KEY
}

object CreateEventDestination: Destination {
    override val argsRoute: String = "create"
    override val baseRoute: String = "create"
    override val icon: ImageVector? = null
    override val label: Int? = null
    override val title: Int = R.string.create_event
}
