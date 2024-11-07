package com.fraro.sample_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fraro.sample_app.ui.screens.PreparationScreen
import com.fraro.sample_app.ui.screens.SimulationScreen
import com.fraro.sample_app.ui.theme.ComposablerealtimeanimationsTheme
import com.fraro.sample_app.ui.viewmodels.MainViewModel
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposablerealtimeanimationsTheme {
                navController = rememberNavController()
                NavHost(
                    navController = navController as NavHostController,
                    startDestination = PreparationRoute
                ) {
                    composable<PreparationRoute> {
                        PreparationScreen(
                            onSubmitClick = {
                                navController.navigate(SimulationRoute)
                            }
                        )
                    }
                    composable<SimulationRoute> {
                        SimulationScreen()
                    }
                }
            }
        }
    }
}

@Serializable
object PreparationRoute

@Serializable
object SimulationRoute