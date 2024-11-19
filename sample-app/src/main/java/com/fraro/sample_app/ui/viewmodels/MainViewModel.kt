package com.fraro.sample_app.ui.viewmodels

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import com.fraro.composable_realtime_animations.data.models.ScreenPosition
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.sample_app.data.CalibrationPoint
import com.fraro.sample_app.data.SimulationActor
import com.fraro.sample_app.data.Trace
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class MainViewModel : ViewModel() {

    val simulationModel = HashMap<Long, SimulationActor>()
    lateinit var animationFlow: Flow<ParticleVisualizationModel>
    val trajectories = HashMap<Long, List<CalibrationPoint>>()

    var screenWidth: Float? = null
    var screenHeight: Float? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startFlow() {
        generateTrajectories()
        // Process movements sequentially for each key, but different keys run concurrently
        /*animationFlow = trajectories.entries.asFlow()
            .flatMapMerge { (key, particles) -> // Concurrently process each particle group
                particles.asFlow()
                    .map { particle -> // Sequential processing within the same key
                        delay(particle.duration.toLong()) // Delay based on the duration
                        println("Emitting particle $key at offset ${particle.screenPosition.offset}")
                        particle
                    }
            }*/
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

    private fun generateDiagonalEquilateralSegmentsPoints(
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
    }

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
                    offset = Offset(
                        x = currentX,
                        y = currentY
                    )
                )
            )
            timeStep++
        }
    }
}
