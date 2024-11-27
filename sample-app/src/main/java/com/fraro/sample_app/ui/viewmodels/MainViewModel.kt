package com.fraro.sample_app.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import com.fraro.composable_realtime_animations.data.models.ScreenPosition
import com.fraro.sample_app.data.CalibrationPoint
import com.fraro.sample_app.data.SimulationActor
import com.fraro.sample_app.data.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class MainViewModel : ViewModel() {

    val simulationModel = HashMap<Long, SimulationActor>()
    lateinit var backwardFlow: Flow<ParticleVisualizationModel>
    lateinit var trajectories: Map<Long, List<CalibrationPoint>>

    val timer = Timer()

    var screenWidth: Float? = null
    var screenHeight: Float? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startFlow() {
        trajectories = generateTrajectories()
        val durations = generateDelays(trajectories)
        backwardFlow = trajectories.entries.asFlow()
            .flatMapMerge { (key, points) ->
                points.asFlow()
                    .map { point ->
                        val delayFractionPrev = durations[key - 1]?.getOrNull(point.order) ?: 0F
                        val delayFactor = durations[key]?.getOrNull(point.order) ?: 0F
                        val duration = 1000L + (1000 * delayFactor).toLong()
                        val color = simulationModel[key]!!.color
                        val shape = simulationModel[key]!!.shape
                        delay(1000L)

                        println("flow iniziale ${point.screenPosition.offset}")

                        ParticleVisualizationModel(
                            id = key,
                            screenPosition = point.screenPosition,
                            duration = duration.toInt(),
                            delayFactor = delayFactor,
                            directionUnitVector = null,
                            shape = shape,
                            color = color
                        )

                    }
            }
    }

    private fun generateDelays(trajectories: Map<Long, List<CalibrationPoint>>): Map<Long, List<Float>> {
        val map = HashMap<Long, MutableList<Float>>()
        trajectories.keys.forEach { key ->
            map[key] = mutableListOf()
        }
        map.values.forEach {
            val delayFraction = Random.nextDouble(50.0, 100.0).toFloat()
            it += delayFraction
        }
        return map
    }

    private fun generateTrajectories(): Map<Long, List<CalibrationPoint>> {
        val trajectories = mutableMapOf<Long, List<CalibrationPoint>>()
        simulationModel.forEach {
            when (it.value.trace) {
                Trace.DIAGONAL -> {
                    trajectories[it.key] = generateAcceleratingTrajectory(
                        screenWidth = screenWidth!! * 2,
                        screenHeight = screenHeight!! * 2,
                        minSpeed = it.value.speed.first.toFloat(),
                        maxSpeed = it.value.speed.second.toFloat(),
                        key = it.key
                    )
                }
                else -> {

                }
            }
        }
        return trajectories.toMap()
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

    private fun generateAcceleratingTrajectory(
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
        result.add(CalibrationPoint(
            id = key,
            order = 0,
            screenPosition = ScreenPosition(
                offset = Offset(
                    x = currentX,
                    y = currentY
                ),
                heading = 0F
            )
        ))
        var currentSpeed = minSpeed

        var timeStep = 1
        var isMaxReached = false
        while (!isMaxReached) {
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
            if (currentX >= screenWidth || currentY >= screenHeight) {
                isMaxReached = true
                if (currentX > screenWidth) {
                    currentX = screenWidth
                    println("larghezza $screenWidth")
                }
                if (currentY > screenHeight) {
                    currentY = screenHeight
                    println("lunghezza $screenHeight")
                }
            }

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
        return result
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }

    inner class Timer {
        private val _timer = MutableStateFlow(0L)
        val timer = _timer.asStateFlow()

        private var timerJob: Job? = null

        fun startTimer() {
            stopTimer()
            //timerJob?.cancel()
            timerJob = CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    delay(1000)
                    _timer.value++
                }
            }
        }

        fun pauseTimer() {
            timerJob?.cancel()
        }

        fun stopTimer() {
            _timer.value = 0
            timerJob?.cancel()
        }

        fun cancel() {
            timerJob?.cancel()
        }
    }
}
