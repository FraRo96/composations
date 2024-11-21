package com.fraro.sample_app.data

import androidx.compose.ui.geometry.Offset
import com.fraro.composable_realtime_animations.data.models.ScreenPosition

data class CalibrationPoint(
    val id: Long,
    val order: Int,
    val screenPosition: ScreenPosition
)

data class Speed(
    val hSpeed: Float,
    val vSpeed: Float
)