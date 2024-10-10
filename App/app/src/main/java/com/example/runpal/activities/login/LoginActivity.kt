package com.example.runpal.activities.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.runpal.LoadingScreen
import com.example.runpal.MAX_IMAGE_SIZE
import com.example.runpal.R
import com.example.runpal.ServerException
import com.example.runpal.repositories.LoginManager
import com.example.runpal.activities.home.HomeActivity
import com.example.runpal.repositories.user.LocalUserRepository
import com.example.runpal.repositories.user.ServerUserRespository
import com.example.runpal.ui.theme.RunPalTheme
import com.example.runpal.ui.theme.StandardNavBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {


    @Inject
    lateinit var loginManager: LoginManager
    @Inject
    lateinit var serverUserRespository: ServerUserRespository
    @Inject
    lateinit var localUserRepository: LocalUserRepository

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
                    val startDestination = LoginDestination.argsRoute
                    val curDestination = navController.currentBackStackEntryAsState().value?.destination?.route ?: startDestination

                    Scaffold(
                        bottomBar = {
                            StandardNavBar(destinations = navBarDestinations, curRoute = curDestination, onClick = {
                                if (curDestination != it.argsRoute) navController.navigate(it.argsRoute)
                            })
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NavHost(navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier
                                .padding(it)
                                .fillMaxSize()) {
                            composable(route = LoginDestination.argsRoute) {
                                var error by remember {
                                    mutableStateOf("")
                                }
                                var loading by rememberSaveable {
                                    mutableStateOf(false)
                                }
                                val scope = rememberCoroutineScope()
                                LoginScreen(onLogin = { email, password ->
                                    scope.launch {
                                        loading = true
                                        try {
                                            loginManager.login(email, password)
                                            val user = serverUserRespository.getUser(email)
                                            localUserRepository.upsert(user)
                                            this@LoginActivity.startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                        } catch (e: ServerException) {
                                            error = e.message ?: ""
                                        } catch (e: Exception) {
                                            Toast.makeText(this@LoginActivity, this@LoginActivity.getString(R.string.no_internet_message), Toast.LENGTH_SHORT).show()
                                            e.printStackTrace()
                                        }
                                        loading = false
                                    }
                                }, errorMessage = error,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp)
                                )
                                if (loading) LoadingScreen()
                            }
                            composable(route = RegisterDestination.argsRoute) {
                                var error by remember {
                                    mutableStateOf("")
                                }
                                var loading by rememberSaveable {
                                    mutableStateOf(false)
                                }
                                val scope = rememberCoroutineScope()
                                RegisterScreen(onRegister = { email, password, name, last, weight, uri ->
                                    loading = true
                                    scope.launch {
                                        try {
                                            loginManager.register(email, password, name, last, weight, uri)
                                            val user = serverUserRespository.getUser(email)
                                            localUserRepository.upsert(user)
                                            this@LoginActivity.startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    HomeActivity::class.java
                                                )
                                            )
                                        } catch (e: ServerException) {
                                            error = e.message ?: ""
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        loading = false
                                    }
                                }, errorMessage = error,
                                    modifier = Modifier.padding(20.dp)
                                )
                                if (loading) LoadingScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

