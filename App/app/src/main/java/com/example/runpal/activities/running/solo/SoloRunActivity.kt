package com.example.runpal.activities.running.solo

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
import com.example.runpal.RUN_ID_KEY
import com.example.runpal.RUN_MARKER_COLORS
import com.example.runpal.activities.results.solo.SoloRunResultsActivity
import com.example.runpal.activities.running.GoogleMapRun
import com.example.runpal.activities.running.RunDataPanel
import com.example.runpal.activities.running.RunPause
import com.example.runpal.activities.running.RunResume
import com.example.runpal.activities.running.RunStart
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
class SoloRunActivity : ComponentActivity() {


    val vm: SoloRunViewModel by viewModels()
    lateinit var serviceIntent: Intent
    val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            (service as LocationService.LocalBinder).setListener { vm.updateLocation(it) }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    var shortbeep: MediaPlayer? = null
    var longbeep: MediaPlayer? = null

    @Inject
    lateinit var settingsManager: SettingsManager

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

                    var units by remember {
                        mutableStateOf(settingsManager.units)
                    }
                    var pace by remember {
                        mutableStateOf(false)
                    }

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        RunDataPanel(runState = vm.runState,
                            units = units, onChangeUnits = {units = units.next},
                            pace = pace, onChangePace = {pace = !pace},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        Box(modifier = Modifier.fillMaxSize()) {

                            GoogleMapRun(
                                runStates = listOf(vm.runState),
                                markers = listOf(vm.marker),
                                colors = RUN_MARKER_COLORS,
                                mapState = vm.mapState,
                                onCenterSwitch = vm::centerSwitch,
                                modifier = Modifier.fillMaxSize())

                            val state = vm.runState.run.state
                            if (state == Run.State.LOADING) LoadingScreen()
                            else if (state == Run.State.READY) RunStart(onStart = {
                                vm.start()
                                longbeep?.start()
                            }, sound = shortbeep)
                            else if (state == Run.State.RUNNING) RunPause(onPause = vm::pause, onFinish = vm::end)
                            else if (state == Run.State.PAUSED) RunResume(onResume = vm::resume, onFinish = vm::end)
                            LaunchedEffect(key1 = state) {
                                if (state == Run.State.ENDED) {
                                    delay(200) //giving time for the server update
                                    val runID = vm.runState.run.id
                                    this@SoloRunActivity.finish()
                                    val intent = Intent(this@SoloRunActivity, SoloRunResultsActivity::class.java)
                                    intent.putExtra(RUN_ID_KEY, runID)
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