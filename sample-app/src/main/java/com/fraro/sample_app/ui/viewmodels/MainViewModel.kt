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
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

class MainViewModel : ViewModel() {

    val simulationModel = HashMap<Long, SimulationActor>()
    lateinit var animationFlow: Flow<ParticleVisualizationModel>
    val trajectories = HashMap<Long, List<ParticleVisualizationModel>>()

    var screenWidth: Float? = null
    var screenHeight: Float? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startFlow() {
        generateTrajectories()
        // Process movements sequentially for each key, but different keys run concurrently
        animationFlow = trajectories.entries.asFlow()
            .flatMapMerge { (key, particles) -> // Concurrently process each particle group
                particles.asFlow()
                    .map { particle -> // Sequential processing within the same key
                        delay(particle.duration.toLong()) // Delay based on the duration
                        println("Emitting particle $key at offset ${particle.screenPosition.offset}")
                        particle
                    }
            }
    }

    private fun generateTrajectories() {
        simulationModel.forEach {
            when (it.value.trace) {
                Trace.DIAGONAL -> {
                    trajectories[it.key] = generateDiagonalEquilateralSegmentsPoints(
                        screenWidth = screenWidth!!,
                        screenHeight = screenHeight!!,
                        simulationActor = it.value,
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
    ): List<ParticleVisualizationModel> {
        val numberOfPoints = 100
        return List(numberOfPoints) { index ->
            val fraction = index / (numberOfPoints - 1).toFloat()
            ParticleVisualizationModel(
                id = key,
                duration = 1000,
                shape = simulationActor.shape,
                color = simulationActor.color,
                screenPosition = ScreenPosition(
                    offset = Offset(
                        x = screenWidth * fraction,
                        y = screenHeight * fraction
                    ),
                    heading = 0F
                )
            )
        }
    }

}
