package com.fraro.sample_app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.fraro.composable_realtime_animations.ui.viewmodels.RealtimeAnimationViewModel
import com.fraro.sample_app.data.SimulationModel
import com.fraro.sample_app.ui.viewmodels.MainViewModel

@Composable
fun SimulationScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: MainViewModel = ViewModelProvider(lifecycleOwner)[MainViewModel::class.java]
}