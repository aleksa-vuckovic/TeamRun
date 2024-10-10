package com.example.runpal.activities.results.solo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.runpal.ErrorScreen
import com.example.runpal.KcalFormatter
import com.example.runpal.LoadingScreen
import com.example.runpal.LocalDateTimeFormatter
import com.example.runpal.RUN_MARKER_COLORS
import com.example.runpal.TimeFormatter
import com.example.runpal.activities.account.AccountActivity
import com.example.runpal.activities.results.GeneralResults
import com.example.runpal.activities.results.PathChartAndPanel
import com.example.runpal.repositories.LoginManager
import com.example.runpal.repositories.SettingsManager
import com.example.runpal.restartApp
import com.example.runpal.AxesOptions
import com.example.runpal.DateOnlyFormatter
import com.example.runpal.ui.theme.RunPalTheme
import com.example.runpal.ui.theme.StandardNavBar
import com.example.runpal.ui.theme.StandardTopBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.runpal.R
import com.example.runpal.TimeOnlyFormatter
import com.example.runpal.ui.theme.BadgeType
import com.example.runpal.ui.theme.StandardBadge

@AndroidEntryPoint
class SoloRunResultsActivity : ComponentActivity() {

    @Inject
    lateinit var loginManager: LoginManager
    @Inject
    lateinit var settingsManager: SettingsManager

    val vm: SoloRunResultsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val units = settingsManager.units

        setContent {
            RunPalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = ResultsDestination.argsRoute
                    val curRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: startDestination
                    val curDestination = destinationsMap[curRoute]!!
                    Scaffold(
                        topBar = {
                            StandardTopBar(
                                onBack =  { if (!navController.popBackStack()) finish()},
                                onRefresh =  vm::reload,
                                onAccount = {startActivity(Intent(this@SoloRunResultsActivity, AccountActivity::class.java))},
                                onLogout = {
                                    loginManager.logout()
                                    this@SoloRunResultsActivity.restartApp()
                                },
                                title = stringResource(id = curDestination.title!!),
                                counterTitle = { if (vm.state == SoloRunResultsViewModel.State.LOADED) StandardBadge(text = DateOnlyFormatter.format(vm.run.run.start!!).first, type = BadgeType.CLASSIC)}
                            )
                        },
                        bottomBar = {
                            if (vm.state == SoloRunResultsViewModel.State.LOADED) StandardNavBar(
                                destinations = bottomBarDestinations,
                                curRoute = curRoute,
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
                        }
                    ) {

                        if (vm.state == SoloRunResultsViewModel.State.LOADING) LoadingScreen()
                        else if (vm.state == SoloRunResultsViewModel.State.ERROR) ErrorScreen(message = "Try again.", modifier = Modifier.padding(it))
                        else NavHost(navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(it)) {

                            composable(route = ChartsDestination.argsRoute) {

                                val axesOptions = AxesOptions(
                                    labelStyle = MaterialTheme.typography.labelSmall,
                                    yTickCount = 5
                                )
                                var pace by remember { mutableStateOf(false) }
                                Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(10.dp)) {
                                    PathChartAndPanel(
                                        title = stringResource(id = if (pace) R.string.pace else R.string.speed),
                                        datasets = listOf(vm.speedDataset),
                                        selected = listOf(true),
                                        axesOptions = axesOptions.copy(
                                            yLabel = if (pace) units.pace.formatter else units.speed.formatter,
                                            yTickCount = if (pace) 0 else 5
                                        ),
                                        onClick = {pace = !pace},
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(400.dp)
                                    )
                                    PathChartAndPanel(
                                        title = stringResource(id = R.string.kcal),
                                        datasets = listOf(vm.kcalDataset),
                                        selected = listOf(true),
                                        axesOptions = axesOptions.copy(yLabel = KcalFormatter),
                                        cumulative = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(400.dp)
                                    )
                                    PathChartAndPanel(
                                        title = stringResource(id = R.string.altitude),
                                        datasets = listOf(vm.altitudeDataset),
                                        selected = listOf(true),
                                        axesOptions = axesOptions.copy(yLabel = units.distance.formatter, ySpanMin = 100.0),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(400.dp)
                                    )
                                    PathChartAndPanel(
                                        title = stringResource(id = R.string.distance),
                                        datasets = listOf(vm.distanceDataset),
                                        selected = listOf(true),
                                        axesOptions = axesOptions.copy(yLabel = units.distance.formatter),
                                        cumulative = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(400.dp)
                                    )
                                }
                            }

                            composable(route = ResultsDestination.argsRoute) {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    GeneralResults(
                                        runData = vm.run,
                                        color = RUN_MARKER_COLORS[0],
                                        values = listOf(
                                            stringResource(id = R.string.total_distance) to units.distance.formatter.format(vm.distanceDataset.maxY),
                                            stringResource(id = R.string.start_time) to (if (vm.run.run.start != null) TimeOnlyFormatter.format(vm.run.run.start!!) else "TBD" to ""),
                                            stringResource(id = R.string.running_time) to TimeFormatter.format(vm.run.run.running),
                                            stringResource(id = R.string.finish_time) to (if (vm.run.run.end != null) TimeOnlyFormatter.format(vm.run.run.end!!) else "TBD" to ""),
                                            stringResource(id = R.string.avg_pace) to units.pace.formatter.format(vm.speedDataset.avgY),
                                            stringResource(id = R.string.max_pace) to units.pace.formatter.format(vm.speedDataset.maxY),
                                            stringResource(id = R.string.total_kcal) to KcalFormatter.format(vm.kcalDataset.maxY)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}