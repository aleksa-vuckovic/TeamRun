package com.example.runpal.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.runpal.Drawer
import com.example.runpal.LoadingDots
import com.example.runpal.MetricDistanceFormatter
import com.example.runpal.R
import com.example.runpal.activities.home.EventPathInputMap
import com.example.runpal.activities.home.HomeActivity
import com.example.runpal.activities.login.LoginActivity
import com.example.runpal.activities.running.MapState
import com.example.runpal.repositories.LoginManager
import com.example.runpal.activities.running.PanelText
import com.example.runpal.risingDoubleAsState
import com.example.runpal.models.PathPoint
import com.example.runpal.repositories.run.CombinedRunRepository
import com.example.runpal.room.PathDao
import com.example.runpal.room.RunDao
import com.example.runpal.room.SyncDao
import com.example.runpal.server.LiveRankingApi
import com.example.runpal.server.LiveRankingApiFactory
import com.example.runpal.ui.theme.RunPalTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var loginManager: LoginManager
    @Inject
    lateinit var combinedRunRepository: CombinedRunRepository

    @Inject
    lateinit var runDao: RunDao
    @Inject
    lateinit var pathDao: PathDao
    @Inject
    lateinit var syncDao: SyncDao

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            syncDao.deleteAll()
            pathDao.deleteAll()
            runDao.deleteAll()
        }

        if (loginManager.logged()) startActivity(Intent(this@MainActivity, HomeActivity::class.java))
        else startActivity(Intent(this@MainActivity, LoginActivity::class.java))
    }


    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RunPalTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {


                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(painter = painterResource(id = R.drawable.runner),
                            contentDescription = "Loading",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(140.dp))
                        LoadingDots(size = 30.dp, count = 3, modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 30.dp))
                    }

                }
            }
        }
    }
}