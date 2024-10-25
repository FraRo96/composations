package com.fraro.sample_app.data

import com.fraro.composable_realtime_animations.data.models.Shape
import kotlinx.serialization.Serializable

@Serializable
data class SimulationModel(
    val a: Int
    //val simulationDataList: List<SimulationActor>
)

@Serializable
data class SimulationActor(
    //val shape: Shape,
    val trace: Trace,
    //val rotation: Rotation,
    val constantSpeed: Boolean,
    val constantAcceleration: Boolean
)

@Serializable
sealed interface Rotation {
    @Serializable
    object Fixed : Rotation
    @Serializable
    data class Rotating(val maxDegrees: Float, val isClockwise: Boolean)
}

@Serializable
enum class Trace {
    FIXED, BORDERS, DIAGONAL, RANDOMIZED
}