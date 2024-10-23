package com.fraro.composable_realtime_animations.data.models

import androidx.compose.ui.geometry.Offset

data class ParticleVisualizationModel(
    val id: Long,
    val screenPosition: ScreenPosition,
    val duration: Int
)

data class ScreenPosition(
    val offset: Offset,
    val heading: Float
)