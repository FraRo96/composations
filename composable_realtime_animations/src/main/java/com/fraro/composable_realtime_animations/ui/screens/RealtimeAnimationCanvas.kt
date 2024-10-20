package com.fraro.composable_realtime_animations.ui.screens

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import com.fraro.composable_realtime_animations.ui.viewmodels.RealtimeAnimationViewModel
import kotlinx.coroutines.flow.Flow

@Composable
fun RealtimeAnimationCanvas(
    animationFlow: Flow<ParticleVisualizationModel>,
    samplingRate: Int,
    iconsBitmap: Map<Int, Bitmap>
) {

    val viewModel: RealtimeAnimationViewModel = hiltViewModel()

    val particlesMap = remember {
        mutableStateMapOf<Int, ParticleVisualizationModel>()
    }

    val collectedFlow = viewModel.animationFlow.collectAsStateWithLifecycle(
        initialValue = null,
        minActiveState = Lifecycle.State.RESUMED
    )

    viewModel.generateStream(
        animationFlow,
        samplingRate
    )

    collectedFlow.value?.let {

    }
}