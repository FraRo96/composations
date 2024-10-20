package com.fraro.composable_realtime_animations.data.models

import androidx.compose.ui.geometry.Offset

data class ParticleVisualizationModel(
    val id: Long,
    val offset: Offset,
    val heading: Double?,
    val duration: Double
)