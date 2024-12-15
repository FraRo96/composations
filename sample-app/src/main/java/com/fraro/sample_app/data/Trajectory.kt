package com.fraro.sample_app.data

import androidx.compose.ui.geometry.Offset
import com.fraro.composable_realtime_animations.data.models.ScreenPosition

data class TrajectoryPoint(
    val id: Long,
    val offset: Offset,
    val deltaTime: Int
)