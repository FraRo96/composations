package com.fraro.sample_app.ui.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import com.fraro.composable_realtime_animations.data.models.ScreenPosition
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.composable_realtime_animations.ui.screens.toBatchedStateFlow
import com.fraro.sample_app.data.CalibrationPoint
import com.fraro.sample_app.data.TrajectoryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SampleViewModel: ViewModel() {

    val animationEmitter = AnimationEmitter()
    val animationTimer = Timer()
    val dragTimer = Timer()

    inner class AnimationEmitter {

        private val _animationFlow = MutableSharedFlow<ParticleVisualizationModel>() // Source of particles
        private val batchedAnimationStateFlow = _animationFlow.toBatchedStateFlow(50L)

        fun getTransformedFlow(): StateFlow<Map<Long, ParticleVisualizationModel>>
                = batchedAnimationStateFlow

        fun emitPoint(point: TrajectoryPoint) {
            CoroutineScope(Dispatchers.Default).launch {
                _animationFlow.emit(
                    ParticleVisualizationModel(
                        id = point.id,
                        screenPosition = ScreenPosition(point.offset, 0f),
                        duration = point.deltaTime.toInt(),
                        delayFactor = 0F,//delayFactor,
                        directionUnitVector = null,
                        shape = Shape.Rectangle(),
                        color = Color.Red
                    )
                )
            }
        }

        fun emitTrajectory(trajectory: List<TrajectoryPoint>) {
            viewModelScope.launch {
                trajectory.forEach { point ->
                    println("nuova posizione: $point")
                    _animationFlow.emit(
                        ParticleVisualizationModel(
                            id = point.id,
                            screenPosition = ScreenPosition(point.offset, 0f),
                            duration = point.deltaTime,
                            delayFactor = 0F,//delayFactor,
                            directionUnitVector = null,
                            shape = Shape.Rectangle(),
                            color = Color.Red
                        )
                    )
                    delay(point.deltaTime.toLong())
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        fun emitMultipleTrajectories(
            trajectories: Map<Long, List<TrajectoryPoint>>) {
            CoroutineScope(Dispatchers.Default).launch {
                trajectories.entries.asFlow()
                    .flatMapMerge { (key, points) ->
                        points.asFlow()
                            .map { point ->
                                ParticleVisualizationModel(
                                    id = point.id,
                                    screenPosition = ScreenPosition(point.offset, 0f),
                                    duration = point.deltaTime.toInt(),
                                    delayFactor = 0F,//delayFactor,
                                    directionUnitVector = null,
                                    shape = Shape.Ellipse(),
                                    color = Color.Red
                                )
                            }
                    }
            }
        }
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
