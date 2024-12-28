package com.fraro.sample_app.ui.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.composable_realtime_animations.data.models.State
import com.fraro.composable_realtime_animations.data.models.StateHolder
import com.fraro.composable_realtime_animations.ui.screens.toBatchedStateFlow
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SampleViewModel: ViewModel() {

    val animationEmitter = AnimationEmitter()
    val animationTimer = Timer()
    val dragTimer = Timer()

    inner class AnimationEmitter {

        private val _animationFlow = MutableSharedFlow<StateHolder<*,*>>() // Source of particles
        private val batchedAnimationStateFlow = _animationFlow.toBatchedStateFlow(1000L)

        fun getTransformedFlow(): StateFlow<Map<Long, StateHolder<*,*>>>
                = batchedAnimationStateFlow

        fun emitTrajectory(trajectory: List<StateHolder<*,*>>) {
            viewModelScope.launch {
                trajectory.forEach { point ->
                    println("nuova posizione: ${point.getState().entries}")
                    _animationFlow.emit(
                        point
                    )
                    val delay = when (point.getPartialState()) {
                        is State.Animated -> {
                            (point.getPartialState() as State.Animated).animation.durationMillis
                        }
                        is State.Start -> {
                            (point.getPartialState() as State.Start).visualDescriptor.durationMillis
                        }
                        else -> {
                            0
                        }
                    }
                    delay(delay.toLong())
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
