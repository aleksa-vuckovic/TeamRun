package com.example.runpal.activities.running.group

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.runpal.Destination
import com.example.runpal.ROOM_ID_KEY
import com.example.runpal.R


object EntryDestination: Destination {
    override val argsRoute: String = "entry"
    override val baseRoute: String = "entry"
    override val icon: ImageVector? = null
    override val label: Int? = null
    override val title: Int = R.string.group_run
}

object LobbyDestination: Destination {
    override val argsRoute: String = "lobby/{${ROOM_ID_KEY}}"
    override val baseRoute: String = "lobby/"
    override val icon: ImageVector? = null
    override val label: Int? = null
    override val title: Int = R.string.lobby
    val arg: String = ROOM_ID_KEY
}