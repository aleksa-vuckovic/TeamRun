package com.example.runpal.activities.login

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.runpal.Destination
import com.example.runpal.R


object LoginDestination: Destination {
    override val argsRoute: String = "login"
    override val baseRoute: String = "login"
    override val icon: ImageVector = Icons.AutoMirrored.Filled.Login
    override val label: Int = R.string.login
    override val title: Int = R.string.login
}

object RegisterDestination: Destination {
    override val argsRoute: String = "register"
    override val baseRoute: String = "register"
    override val icon: ImageVector = Icons.Default.HowToReg
    override val label: Int = R.string.register
    override val title: Int = R.string.register
}

val navBarDestinations = listOf(LoginDestination, RegisterDestination)