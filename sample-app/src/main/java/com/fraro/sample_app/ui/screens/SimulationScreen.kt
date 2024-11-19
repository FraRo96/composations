package com.fraro.sample_app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import com.fraro.composable_realtime_animations.ui.screens.RealtimeAnimationCanvas
import com.fraro.sample_app.ui.viewmodels.MainViewModel

@Composable
fun SimulationScreen() {

    val context = LocalContext.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: MainViewModel = ViewModelProvider(lifecycleOwner)[MainViewModel::class.java]

    //RealtimeAnimationCanvas(animationFlow = viewModel.animationFlow, samplingInterval = 100)

    /*
    val particles = remember { mutableStateListOf<ParticleVisualizationModel>() }

    LaunchedEffect(viewModel) {
        viewModel.animationFlow.collect { particle ->
            particles.add(particle)
        }
    }*/

    // Draw particles using Canvas
    Box {
        Canvas(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            viewModel.trajectories.forEach {
                it.value.forEach { it1 ->
                    drawParticle(it1)
                }
            }
        }
    }
}


private fun DrawScope.drawParticle(particle: ParticleVisualizationModel) {
    drawCircle(
        color = particle.color ?: Color.Black, // Default to black if color is null
        center = particle.screenPosition.offset,
        radius = 10.dp.toPx()
    )
}