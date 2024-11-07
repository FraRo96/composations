package com.fraro.sample_app.data

import androidx.compose.ui.graphics.Color
import com.fraro.composable_realtime_animations.data.models.Shape
import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

data class SimulationActor(
    val shape: Shape,
    val trace: Trace,
    val rotation: Pair<Int, Int>,
    val color: Color,
    val isRotationClockwise: Boolean,
    val speed: Pair<Int, Int>,

    )

enum class Trace(val description: String) {
    FIXED("Fixed"),
    BORDERS("Borders"),
    DIAGONAL("Diagonals"),
    RANDOMIZED("Randomized")
}