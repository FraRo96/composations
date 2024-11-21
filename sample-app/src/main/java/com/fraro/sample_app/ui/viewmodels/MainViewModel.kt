package com.fraro.sample_app.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import com.fraro.composable_realtime_animations.data.models.ScreenPosition
import com.fraro.sample_app.data.CalibrationPoint
import com.fraro.sample_app.data.SimulationActor
import com.fraro.sample_app.data.Trace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class MainViewModel : ViewModel() {

    val simulationModel = HashMap<Long, SimulationActor>()
    lateinit var backwardFlow: Flow<ParticleVisualizationModel>
    val trajectories = HashMap<Long, List<CalibrationPoint>>()

    var screenWidth: Float? = null
    var screenHeight: Float? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startFlow() {
        generateTrajectories()
        val durations = generateDelays()
        backwardFlow = trajectories.entries.asFlow()
            .flatMapMerge { (key, points) ->
                points.asFlow()
                    .map { point ->
                        val delayFractionPrev = durations[key - 1]?.getOrNull(point.order) ?: 0F
                        val delayFraction = durations[key]?.getOrNull(point.order) ?: 0F
                        val duration = 1000L + (1000 * delayFraction).toLong()
                        val color = simulationModel[key]!!.color
                        val shape = simulationModel[key]!!.shape
                        delay(1000L + (1000 * delayFractionPrev).toLong())

                        ParticleVisualizationModel(
                            id = key,
                            screenPosition = point.screenPosition,
                            duration = duration.toInt(),
                            maximumDelayFraction = delayFraction,
                            directionUnitVector = null,
                            shape = shape,
                            color = color
                        )

                    }
            }
    }

    private fun generateDelays(): Map<Long, List<Float>> {
        val map = HashMap<Long, MutableList<Float>>()
        trajectories.keys.forEach { key ->
            map[key] = mutableListOf()
        }
        map.values.forEach {
            val delayFraction = Random.nextDouble(0.0, 1.0).toFloat()
            it += delayFraction
        }
        return map
    }

    private fun generateTrajectories() {
        simulationModel.forEach {
            when (it.value.trace) {
                Trace.DIAGONAL -> {
                    trajectories[it.key] = generateDiagonalNonHomogeneousSegmentsPoints(
                        screenWidth = screenWidth!!,
                        screenHeight = screenHeight!!,
                        minSpeed = it.value.speed.first.toFloat(),
                        maxSpeed = it.value.speed.second.toFloat(),
                        key = it.key
                    )
                }
                else -> {

                }
            }
        }
    }

    /*private fun generateDiagonalEquilateralSegmentsPoints(
        screenWidth: Float,
        screenHeight: Float,
        simulationActor: SimulationActor,
        key: Long,
    ): List<CalibrationPoint> {
        val numberOfPoints = 10
        return List(numberOfPoints) { index ->
            val fraction = index / (numberOfPoints - 1).toFloat()
            CalibrationPoint(
                id = key,
                offset = Offset(
                        x = screenWidth * fraction,
                        y = screenHeight * fraction
                    ),
                order = index
            )
        }
    }*/

    private fun generateDiagonalNonHomogeneousSegmentsPoints(
        screenWidth: Float,
        screenHeight: Float,
        key: Long,
        minSpeed: Float,
        maxSpeed: Float
    ): List<CalibrationPoint> {
        // Calculate the diagonal angle theta
        val theta = atan2(screenHeight, screenWidth)
        val cosTheta = cos(theta)
        val sinTheta = sin(theta)

        val timeSteps = (maxSpeed - minSpeed).roundToInt()
        // Speed increment per second to progress from minSpeed to maxSpeed
        val speedIncrement = (maxSpeed - minSpeed) / timeSteps

        // Initialize position
        var currentX = 0f
        var currentY = 0f

        val result = mutableListOf<CalibrationPoint>()
        var currentSpeed = minSpeed

        var timeStep = 0
        while (true) {
            // Update current speed during the initial phase
            if (timeStep < timeSteps) {
                currentSpeed = minSpeed + timeStep * speedIncrement
            }

            // Calculate speed components
            val horizontalSpeed = currentSpeed * cosTheta
            val verticalSpeed = currentSpeed * sinTheta

            // Update position
            currentX += horizontalSpeed
            currentY += verticalSpeed

            // Stop generating points if the position goes out of bounds
            if (currentX > screenWidth || currentY > screenHeight) return result

            result.add(
                CalibrationPoint(
                    id = key,
                    order = timeStep,
                    screenPosition = ScreenPosition(
                        offset = Offset(
                            x = currentX,
                            y = currentY
                        ),
                        heading = 0F
                    )
                )
            )
            timeStep++
        }
    }
}
