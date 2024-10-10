package com.example.runpal.activities.running.group

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.runpal.ROOM_ID_KEY
import com.example.runpal.ServerException
import com.example.runpal.repositories.ServerRoomRepository
import com.example.runpal.ui.theme.RunPalTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GroupRunEntryActivity : ComponentActivity() {

    @Inject
    lateinit var serverRoomRepository: ServerRoomRepository

    val popBackStack = mutableStateOf(false)

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
                    val startDestination = EntryDestination.argsRoute

                    NavHost(navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize()) {

                        composable(route = EntryDestination.argsRoute) {

                            EntryScreen(onJoin = {
                                lifecycleScope.launch {
                                    try {
                                        serverRoomRepository.join(it)
                                        navController.navigate(LobbyDestination.baseRoute + it)
                                    } catch (e: ServerException) {
                                        e.printStackTrace()
                                        Toast.makeText(this@GroupRunEntryActivity, e.message, Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(this@GroupRunEntryActivity, "Can't connect to server. Check your internet connection.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }, onCreate = {
                                lifecycleScope.launch {
                                    try {
                                        val room = serverRoomRepository.create()
                                        navController.navigate(LobbyDestination.baseRoute + room)
                                    } catch (e: ServerException) {
                                        e.printStackTrace()
                                        Toast.makeText(this@GroupRunEntryActivity, e.message, Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(this@GroupRunEntryActivity, "Can't connect to server. Check your internet connection.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxSize())
                        }

                        composable(route = LobbyDestination.argsRoute,
                            arguments = listOf(navArgument(LobbyDestination.arg) {type = NavType.StringType})) {

                            val vm: LobbyViewModel = hiltViewModel()
                            LobbyScreen(
                                room = vm.room,
                                users = vm.users,
                                state = vm.state,
                                onCopy = vm::copy,
                                onLeave = vm::leave,
                                onReady = vm::ready,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp))

                            LaunchedEffect(key1 = vm.state) {
                                if (vm.state == LobbyViewModel.State.LEAVE || vm.state == LobbyViewModel.State.ERROR) {
                                    navController.popBackStack()
                                }
                                else if (vm.state == LobbyViewModel.State.START) {
                                    val intent = Intent(this@GroupRunEntryActivity, GroupRunActivity::class.java)
                                    intent.putExtra(ROOM_ID_KEY, vm.room._id)
                                    finish()
                                    startActivity(intent)
                                }
                            }


                        }
                    }
                }
            }
        }
    }
}
