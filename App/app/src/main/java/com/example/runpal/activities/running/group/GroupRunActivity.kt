package com.example.runpal.activities.running.group

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.runpal.CLASS_NAME_KEY
import com.example.runpal.LOCATION_UPDATE_PERIOD
import com.example.runpal.LoadingScreen
import com.example.runpal.LocationService
import com.example.runpal.PACKAGE_KEY
import com.example.runpal.R
import com.example.runpal.ROOM_ID_KEY
import com.example.runpal.RUN_MARKER_COLORS
import com.example.runpal.activities.results.group.GroupRunResultsActivity
import com.example.runpal.activities.running.GoogleMapRun
import com.example.runpal.activities.running.RunCountown
import com.example.runpal.activities.running.RunDataPanel
import com.example.runpal.activities.running.RunPause
import com.example.runpal.activities.running.RunResume
import com.example.runpal.hasLocationPermission
import com.example.runpal.models.Run
import com.example.runpal.repositories.SettingsManager
import com.example.runpal.ui.theme.RunPalTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject


@AndroidEntryPoint
class GroupRunActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    val vm: GroupRunViewModel by viewModels()
    lateinit var serviceIntent: Intent
    val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            (service as LocationService.LocalBinder).setListener { vm.updateLocation(it) }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    var shortbeep: MediaPlayer? = null
    var longbeep: MediaPlayer? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this.hasLocationPermission()) this.finish()
        serviceIntent = Intent(this, LocationService::class.java).apply {
            putExtra(PACKAGE_KEY, componentName.packageName);
            putExtra(CLASS_NAME_KEY, componentName.className)
        }
        startForegroundService(serviceIntent)
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        shortbeep = MediaPlayer.create(this, R.raw.shortbeep)
        longbeep = MediaPlayer.create(this, R.raw.longbeep)

        setContent {
            RunPalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LaunchedEffect(key1 = vm.state) {
                        if (vm.state == GroupRunViewModel.State.FAILED) finish()
                    }

                    var units by remember {
                        mutableStateOf(settingsManager.units)
                    }
                    var pace by remember {
                        mutableStateOf(false)
                    }

                    if (vm.state == GroupRunViewModel.State.LOADING) LoadingScreen()
                    else if (vm.state == GroupRunViewModel.State.LOADED) Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        RunDataPanel(runState = vm.runStates[0],
                            units = units, onChangeUnits = {units = units.next},
                            pace = pace, onChangePace = {pace = !pace},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp))
                        Box(modifier = Modifier.fillMaxSize()) {

                            GoogleMapRun(
                                runStates = vm.runStates,
                                markers = vm.markers,
                                colors = RUN_MARKER_COLORS,
                                mapState = vm.mapState,
                                onCenterSwitch = vm::centerSwitch,
                                modifier = Modifier.fillMaxSize()
                            )

                            val state = vm.runStates[0].run.state
                            if (state == Run.State.LOADING) LoadingScreen(dotSize = 15.dp)
                            else if (state == Run.State.READY) RunCountown(till = vm.room.start!! + 20000L, onStart = {
                                vm.start()
                                longbeep?.start()
                            }, sound = shortbeep)
                            else if (state == Run.State.RUNNING) {
                                RunPause(onPause = vm::pause, onFinish = vm::end)
                                MapRanking(runStates = vm.runStates, users = vm.users, units = units, pace = pace)
                            }
                            else if (state == Run.State.PAUSED) {
                                RunResume(onResume = vm::resume, onFinish = vm::end)
                                MapRanking(runStates = vm.runStates, users = vm.users, units = units, pace = pace)
                            }
                            LaunchedEffect(key1 = state) {
                                if (state == Run.State.ENDED) {
                                    delay(200) //giving time for the server update
                                    val roomID = intent.getStringExtra(ROOM_ID_KEY)
                                    this@GroupRunActivity.finish()
                                    val intent = Intent(this@GroupRunActivity, GroupRunResultsActivity::class.java)
                                    intent.putExtra(ROOM_ID_KEY, roomID)
                                    startActivity(intent)
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
        stopService(serviceIntent)
        shortbeep?.release()
        longbeep?.release()
        shortbeep = null
        longbeep = null
    }
}