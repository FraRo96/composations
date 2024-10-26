package com.fraro.sample_app.data

import com.fraro.composable_realtime_animations.data.models.Shape
import kotlinx.serialization.Serializable

data class SimulationModel(
    val simulationDataList: List<SimulationActor>
)

data class SimulationActor(
    val shape: Shape,
    val trace: Trace,
    val rotation: Rotation,
    val constantSpeed: Boolean,
    val constantAcceleration: Boolean
)

sealed interface Rotation {
    object Fixed : Rotation
    data class Rotating(val maxDegrees: Float, val isClockwise: Boolean)
}

enum class Trace(val description: String) {
    FIXED("Fixed"),
    BORDERS("Borders"),
    DIAGONAL("Diagonals"),
    RANDOMIZED("Randomized")
}

enum class ShapeCustomization {
    RECT, CUSTOM_SHAPE, POLYGON, ELLIPSE, SEGMENT
}