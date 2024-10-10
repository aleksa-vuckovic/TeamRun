package com.example.runpal.activities.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.runpal.EVENT_DEEP_LINK_URI
import com.example.runpal.EVENT_ID_KEY
import com.example.runpal.ErrorScreen
import com.example.runpal.LoadingScreen
import com.example.runpal.LocalDateTimeFormatter
import com.example.runpal.ROOM_ID_KEY
import com.example.runpal.RUN_ID_KEY
import com.example.runpal.activities.account.AccountActivity
import com.example.runpal.activities.results.event.EventRunResultsActivity
import com.example.runpal.activities.results.group.GroupRunResultsActivity
import com.example.runpal.activities.results.solo.SoloRunResultsActivity
import com.example.runpal.activities.running.event.EventRunActivity
import com.example.runpal.activities.running.group.GroupRunEntryActivity
import com.example.runpal.activities.running.solo.SoloRunActivity
import com.example.runpal.hasLocationPermission
import com.example.runpal.hasNotificationPermission
import com.example.runpal.models.Run
import com.example.runpal.repositories.LoginManager
import com.example.runpal.repositories.SettingsManager
import com.example.runpal.restartApp
import com.example.runpal.ui.theme.RunPalTheme
import com.example.runpal.ui.theme.StandardDialog
import com.example.runpal.ui.theme.StandardNavBar
import com.example.runpal.ui.theme.StandardTopBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.runpal.R
import com.example.runpal.activities.running.group.GroupRunActivity
import com.example.runpal.join
import com.example.runpal.models.RunData
import com.example.runpal.repositories.run.CombinedRunRepository
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    companion object {
        val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    @Inject
    lateinit var loginManager: LoginManager
    @Inject
    lateinit var settingsManager: SettingsManager
    @Inject
    lateinit var runRepository: CombinedRunRepository

    var startIntent: Intent? = null
    val locLauncher =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.size == 2 && this@HomeActivity.startIntent != null) startActivity(this@HomeActivity.startIntent)
    }
    val notifLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> }

    var refresh by mutableStateOf(false)
    var unfinished by mutableStateOf<RunData?>(null)

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val units = settingsManager.units
        if (!this.hasNotificationPermission()) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        lifecycleScope.launch {
            unfinished = runRepository.unfinished()
        }

        setContent {
            RunPalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = MenuDestination.argsRoute
                    val curDestination = navController.currentBackStackEntryAsState().value?.destination?.route ?: startDestination
                    Scaffold(
                        topBar = {
                            StandardTopBar(
                                title = stringResource(destinationsMap[curDestination]!!.title!!),
                                onBack = { if(!navController.popBackStack()) finish() },
                                onAccount = { startActivity(Intent(this@HomeActivity, AccountActivity::class.java)) },
                                onLogout = {
                                    loginManager.logout()
                                    this@HomeActivity.restartApp()
                                },
                                onRefresh = if (curDestination != EventsDestination.argsRoute && curDestination != EventDestination.argsRoute) null
                                            else { -> refresh = true})
                        },
                        bottomBar = {
                            StandardNavBar(
                                destinations = navBarDestinations,
                                curRoute = curDestination,
                                onClick = {
                                    navController.navigate(it.argsRoute) {
                                        popUpTo(navController.graph.id) {
                                            inclusive = false
                                            saveState = true
                                        }
                                        restoreState = true
                                    }
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NavHost(navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(it)) {
                            
                            composable(route = HistoryDestination.argsRoute) {
                                HistoryScreen(onClick = {
                                    val intent: Intent
                                    if (it.room != null) intent = Intent(this@HomeActivity, GroupRunResultsActivity::class.java).apply { putExtra(ROOM_ID_KEY, it.room) }
                                    else if (it.event != null) intent = Intent(this@HomeActivity, EventRunResultsActivity::class.java).apply { putExtra(EVENT_ID_KEY, it.event)}
                                    else intent = Intent(this@HomeActivity, SoloRunResultsActivity::class.java).apply { putExtra(RUN_ID_KEY, it.id) }
                                    startActivity(intent)
                                }, units = units)
                            }

                            composable(route = MenuDestination.argsRoute) {
                                MenuScreen(
                                    onSoloRun = {
                                        startIntent = Intent(this@HomeActivity, SoloRunActivity::class.java)
                                        startIntent?.putExtra(RUN_ID_KEY, Run.UNKNOWN_ID)
                                        if (hasLocationPermission()) startActivity(startIntent)
                                        else locLauncher.launch(PERMISSIONS)
                                    },
                                    onGroupRun = {
                                        startIntent = Intent(this@HomeActivity, GroupRunEntryActivity::class.java)
                                        if (hasLocationPermission()) startActivity(startIntent)
                                        else locLauncher.launch(PERMISSIONS)
                                    },
                                    onEvent = { navController.navigate(EventsDestination.argsRoute)},
                                    modifier = Modifier.fillMaxSize())
                            }

                            composable(route = StatsDestination.argsRoute) {
                                StatsScreen(units = units)
                            }

                            composable(route = EventsDestination.argsRoute) {

                                val vm: EventsViewModel = hiltViewModel()

                                LaunchedEffect(key1 = refresh) {
                                    if (refresh) {
                                        refresh = false
                                        vm.reload()
                                    }
                                }
                                Box(modifier = Modifier.fillMaxSize()) {
                                    EventsScreen(
                                        followedEvents = vm.followedEvents,
                                        searchEvents = vm.otherEvents,
                                        onClick = {
                                            navController.navigate(EventDestination.baseRoute + it._id)
                                        },
                                        onSearch = {
                                            vm.search(it)
                                        },
                                        onCreate = {
                                            navController.navigate(CreateEventDestination.argsRoute)
                                        },
                                        modifier = Modifier.fillMaxSize())
                                    if (vm.state == EventsViewModel.State.LOADING) LoadingScreen()
                                }


                            }
                            composable(route = CreateEventDestination.argsRoute) {

                                val vm: CreateEventViewModel = hiltViewModel()

                                Box(modifier = Modifier.fillMaxSize()) {
                                    CreateEventScreen(
                                        onCreate = { name, desc, time, distance, image, path, tolerance ->
                                            vm.create(name, desc, time, distance, image, path, tolerance)
                                        },
                                        errorMessage = vm.error,
                                        preferredUnits = units,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = 10.dp, end = 10.dp)
                                    )
                                    if (vm.state == CreateEventViewModel.State.WAITING) LoadingScreen()
                                    else if (vm.state == CreateEventViewModel.State.SUCCESS) {
                                        StandardDialog(
                                            text = stringResource(id = R.string.your_event_is_public),
                                            onDismiss = { navController.popBackStack() },
                                            onYes = {navController.popBackStack()})
                                    }
                                }
                                
                            }
                            composable(route = EventDestination.argsRoute,
                                arguments = listOf(
                                    navArgument(name = EventDestination.arg) {type = NavType.StringType}
                                ), deepLinks = listOf( navDeepLink{ uriPattern= EVENT_DEEP_LINK_URI + "{${EVENT_ID_KEY}}"; action=Intent.ACTION_VIEW})) {

                                val vm: EventViewModel = hiltViewModel()

                                LaunchedEffect(key1 = refresh) {
                                    if (refresh) {
                                        refresh = false
                                        vm.reload()
                                    }
                                }

                                if (vm.state == EventViewModel.State.ERROR) ErrorScreen(message = stringResource(id = R.string.no_internet_message))
                                else if (vm.state == EventViewModel.State.LOADING) LoadingScreen()
                                else EventScreen(
                                    event = vm.event,
                                    onJoin = {
                                        startIntent = Intent(this@HomeActivity, EventRunActivity::class.java)
                                        startIntent?.putExtra(EVENT_ID_KEY, vm.event._id)
                                        if (hasLocationPermission()) startActivity(startIntent)
                                        else locLauncher.launch(PERMISSIONS)
                                    },
                                    onFollow = vm::follow,
                                    onUnfollow = vm::unfollow,
                                    units = units
                                )
                            }
                        }
                    }
                    if (unfinished != null) {
                        StandardDialog(
                            text = "${stringResource(R.string.do_you_want_to_continue)} (${
                                units.distance.formatter.format(
                                    unfinished!!.location.distance
                                ).join()
                            }) ${stringResource(R.string.started_at)} ${LocalDateTimeFormatter.format(unfinished!!.run.start!!).join()}?",
                            onDismiss = {
                                val run = unfinished!!.run
                                unfinished = null
                                lifecycleScope.launch { runRepository.delete(run.id); }
                            }, onYes = {
                                val intent: Intent
                                val run = unfinished!!.run
                                unfinished = null
                                if (run.room != null) intent = Intent(this@HomeActivity, GroupRunActivity::class.java).apply { putExtra(ROOM_ID_KEY, run.room) }
                                else if (run.event != null) intent = Intent(this@HomeActivity, EventRunActivity::class.java).apply { putExtra(EVENT_ID_KEY, run.event)}
                                else intent = Intent(this@HomeActivity, SoloRunActivity::class.java).apply { putExtra(RUN_ID_KEY, run.id) }
                                startActivity(intent)
                            }, onNo = {
                                val run = unfinished!!.run
                                unfinished = null
                                lifecycleScope.launch { runRepository.delete(run.id); }
                            },
                            yesText = stringResource(R.string.yes),
                            noText = stringResource(R.string.no)
                        )
                    }
                }
            }
        }
    }
}