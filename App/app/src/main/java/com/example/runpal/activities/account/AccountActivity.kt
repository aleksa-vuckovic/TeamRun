package com.example.runpal.activities.account

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.runpal.ErrorScreen
import com.example.runpal.LoadingScreen
import com.example.runpal.MAX_IMAGE_SIZE
import com.example.runpal.R
import com.example.runpal.repositories.LoginManager
import com.example.runpal.repositories.SettingsManager
import com.example.runpal.restartApp
import com.example.runpal.ui.theme.RunPalTheme
import com.example.runpal.ui.theme.StandardTopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AccountActivity : ComponentActivity() {

    @Inject
    lateinit var loginManager: LoginManager
    @Inject
    lateinit var settingsManager: SettingsManager

    val vm: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RunPalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = AccountDestination.argsRoute
                    val curRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: startDestination
                    val user by vm.user.collectAsState()
                    val state by vm.state.collectAsState()
                    val curDestination = destinationsMap[curRoute]!!
                    Scaffold(
                        topBar = {
                            StandardTopBar(
                                onBack = { if (!navController.popBackStack()) finish() },
                                onAccount = { },
                                onLogout = {
                                    loginManager.logout()
                                    this@AccountActivity.restartApp()
                                },
                                title = stringResource(id = curDestination.title!!)
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state == AccountViewModel.State.LOADED) NavHost(navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(it)) {

                            composable(route = AccountDestination.argsRoute) {

                                var units by remember {
                                    mutableStateOf(settingsManager.units)
                                }

                                AccountScreen(
                                    user = user,
                                    onEdit = {
                                        navController.navigate(EditDestination.argsRoute)
                                    }, units = units,
                                    onSelectUnits = {
                                        settingsManager.units = it
                                        units = it
                                    }, modifier = Modifier.fillMaxSize())
                            }

                            composable(route = EditDestination.argsRoute) {

                                var error by remember {
                                    mutableStateOf("")
                                }

                                EditScreen(init = user,
                                    onUpdate = { name, last, weight, profile ->
                                        lifecycleScope.launch {
                                            vm.update(name, last, weight, if (profile  == user.profileUri) null else profile)
                                        }
                                        navController.navigate(AccountDestination.argsRoute)
                                    }, errorMessage = error,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp))
                            }
                        }
                        else if (state == AccountViewModel.State.LOADING) LoadingScreen()
                        else ErrorScreen(message = stringResource(id = R.string.no_internet_message))
                    }
                }
            }
        }
    }
}