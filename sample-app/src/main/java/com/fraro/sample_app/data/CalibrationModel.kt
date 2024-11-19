package com.fraro.sample_app.data

import androidx.compose.ui.geometry.Offset

data class CalibrationPoint(
    val id: Long,
    val order: Int,
    val offset: Offset
)