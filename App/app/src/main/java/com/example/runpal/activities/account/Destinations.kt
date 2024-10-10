package com.example.runpal.activities.account

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.runpal.Destination
import com.example.runpal.R

object AccountDestination: Destination {
    override val argsRoute: String = "account"
    override val baseRoute: String = "account"
    override val icon: ImageVector? = null
    override val label: Int? = null
    override val title: Int = R.string.account
}

object EditDestination: Destination {
    override val argsRoute: String = "edit"
    override val baseRoute: String = "edit"
    override val icon: ImageVector? = null
    override val label: Int? = null
    override val title: Int = R.string.edit
}

val destinationsMap = mapOf<String, Destination>(
    AccountDestination.argsRoute to AccountDestination,
    EditDestination.argsRoute to EditDestination
)