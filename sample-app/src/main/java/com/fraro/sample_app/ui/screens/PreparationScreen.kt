package com.fraro.sample_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.sample_app.SimulationRoute
import com.fraro.sample_app.data.Rotation
import com.fraro.sample_app.data.SimulationActor
import com.fraro.sample_app.data.SimulationModel
import com.fraro.sample_app.data.Trace

@Composable
fun PreparationScreen(
    onSubmitClick: () -> Unit
) {

    val navController = rememberNavController()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(onClick = {
            onSubmitClick()
        }) {
            Text(text = "Go to simulation!")
        }
    }
}