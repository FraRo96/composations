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
import com.fraro.sample_app.ui.screens.SampleScreen
import com.fraro.sample_app.ui.theme.ComposablerealtimeanimationsTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposablerealtimeanimationsTheme {
                SampleScreen()
            }
        }
    }
}

@Serializable
object PreparationRoute

@Serializable
object SimulationRoute