package com.example.runpal.activities.results.group

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.runpal.activities.results.UserSelection
import com.example.runpal.borderBottom
import com.example.runpal.repositories.LoginManager
import com.example.runpal.repositories.SettingsManager
import com.example.runpal.restartApp
import com.example.runpal.AxesOptions
import com.example.runpal.DateOnlyFormatter
import com.example.runpal.ui.theme.RunPalTheme
import com.example.runpal.ui.theme.StandardNavBar
import com.example.runpal.ui.theme.StandardSpinner
import com.example.runpal.ui.theme.StandardTopBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.runpal.R
import com.example.runpal.TimeOnlyFormatter
import com.example.runpal.ui.theme.BadgeType
import com.example.runpal.ui.theme.StandardBadge

@AndroidEntryPoint
class GroupRunResultsActivity : ComponentActivity() {

    @Inject
    lateinit var loginManager: LoginManager
    @Inject
    lateinit var settingsManager: SettingsManager

    val vm: GroupRunResultsViewModel by viewModels()

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
                                onAccount = {startActivity(Intent(this@GroupRunResultsActivity, AccountActivity::class.java))},
                                onLogout = {
                                    loginManager.logout()
                                    this@GroupRunResultsActivity.restartApp()
                                },
                                title = stringResource(id = curDestination.title!!),
                                counterTitle = { if (vm.state == GroupRunResultsViewModel.State.LOADED) StandardBadge(text = DateOnlyFormatter.format(vm.runs[0].run.start!!).first, type = BadgeType.CLASSIC) }
                            )
                        },
                        bottomBar = {
                            if (vm.state == GroupRunResultsViewModel.State.LOADED) StandardNavBar(
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
                        if (vm.state == GroupRunResultsViewModel.State.LOADING) LoadingScreen()
                        else if (vm.state == GroupRunResultsViewModel.State.ERROR) ErrorScreen(message = "Try again.")
                        else NavHost(navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.padding(it)) {

                            composable(route = ChartsDestination.argsRoute) {

                                val selected = remember(vm.users){
                                    val res = mutableStateListOf<Boolean>()
                                    res.addAll(vm.users.map { true })
                                    res
                                }
                                val axesOptions = AxesOptions(
                                    labelStyle = MaterialTheme.typography.labelSmall,
                                    yTickCount = 5
                                )
                                var pace by remember { mutableStateOf(false) }

                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    UserSelection(
                                        users = vm.users,
                                        selected = selected,
                                        onSelect = {selected[it] = !selected[it]},
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Column(modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(10.dp)) {
                                        PathChartAndPanel(
                                            title = stringResource(id = if (pace) R.string.pace else R.string.speed),
                                            datasets = vm.speedDatasets,
                                            selected = selected,
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
                                            datasets = vm.kcalDatasets,
                                            selected = selected,
                                            axesOptions = axesOptions.copy(yLabel = KcalFormatter),
                                            cumulative = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(400.dp)
                                        )
                                        PathChartAndPanel(
                                            title = stringResource(id = R.string.altitude),
                                            datasets = vm.altitudeDatasets,
                                            selected = selected,
                                            axesOptions = axesOptions.copy(yLabel = units.distance.formatter, ySpanMin = 100.0),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(400.dp)
                                        )
                                        PathChartAndPanel(
                                            title = stringResource(id = R.string.distance),
                                            datasets = vm.distanceDatasets,
                                            selected = selected,
                                            axesOptions = axesOptions.copy(yLabel = units.distance.formatter),
                                            cumulative = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(400.dp)
                                        )
                                    }

                                }
                            }

                            composable(route = ResultsDestination.argsRoute) {
                                var selected by remember{ mutableStateOf(0) }
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    UserSelection(
                                        users = vm.users,
                                        selected = List(vm.users.size) {it == selected},
                                        onSelect = {selected = it},
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    GeneralResults(
                                        runData = vm.runs[selected],
                                        color = RUN_MARKER_COLORS[selected],
                                        values = listOf(
                                            stringResource(id = R.string.total_distance) to units.distance.formatter.format(vm.distanceDatasets[selected].maxY),
                                            stringResource(id = R.string.start_time) to (if (vm.runs[selected].run.start != null) TimeOnlyFormatter.format(vm.runs[selected].run.start!!) else "TBD" to ""),
                                            stringResource(id = R.string.running_time) to TimeFormatter.format(vm.runs[selected].run.running),
                                            stringResource(id = R.string.finish_time) to (if (vm.runs[selected].run.end != null) TimeOnlyFormatter.format(vm.runs[selected].run.end!!) else "TBD" to ""),
                                            stringResource(id = R.string.avg_pace) to units.pace.formatter.format(vm.speedDatasets[selected].avgY),
                                            stringResource(id = R.string.max_pace) to units.pace.formatter.format(vm.speedDatasets[selected].maxY),
                                            stringResource(id = R.string.total_kcal) to KcalFormatter.format(vm.kcalDatasets[selected].maxY)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            composable(route = RankingDestination.argsRoute) {
                                val distance = stringResource(id = R.string.distance)
                                val pace = stringResource(id = R.string.pace)
                                val speed = stringResource(id = R.string.speed)
                                val kcal = stringResource(id = R.string.kcal)
                                val possibleCriteria = remember {
                                    listOf(distance, pace, speed, kcal)
                                }
                                var criteria by rememberSaveable {
                                    mutableStateOf(distance)
                                }
                                val ranking = remember(criteria) {
                                    if (criteria == distance)
                                        vm.runs.zip(vm.users).sortedBy {-it.first.location.distance}.map {
                                            it.second to units.distance.formatter.format(it.first.location.distance)
                                        }
                                    else if (criteria == kcal)
                                        vm.runs.zip(vm.users).sortedBy {-it.first.location.kcal}.map {
                                            it.second to KcalFormatter.format(it.first.location.kcal)
                                        }
                                    else
                                        vm.speedDatasets.zip(vm.users).sortedBy { -it.first.avgY }.map {
                                            it.second to (if (criteria == speed) units.speed.formatter.format(it.first.avgY) else units.pace.formatter.format(it.first.avgY))
                                        }
                                }
                                Column(modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .borderBottom(outer = true)
                                            .padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                       Text(text = stringResource(id = R.string.criteria) + ": ")
                                       StandardSpinner(
                                           values = possibleCriteria,
                                           selected = criteria,
                                           onSelect = {criteria = it}
                                       )
                                    }
                                    for (i in ranking.indices) {
                                        UserRankingRow(value = ranking[i].second,
                                            user = ranking[i].first,
                                            rank = i+1
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
}
