package com.fraro.composable_realtime_animations.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import com.fraro.composable_realtime_animations.domain.AnimationDataStreamerUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class RealtimeAnimationViewModel : ViewModel() {

    lateinit var animationFlow: StateFlow<ConcurrentHashMap<Long, ParticleVisualizationModel>?>
    val animationDataStreamerUseCase = AnimationDataStreamerUseCase()

    @OptIn(FlowPreview::class)
    private fun transformStream(
        samplingInterval: Int
    ): Flow<ConcurrentHashMap<Long, ParticleVisualizationModel>> {
        val particleMap = ConcurrentHashMap<Long, ParticleVisualizationModel>()

        return animationDataStreamerUseCase.streamFlow
            .filterNotNull()
            .onEach { particleModel ->
                particleMap[particleModel.id] = particleModel
            }
            .sample(samplingInterval.toLong())
            .map {
                val snapshot = ConcurrentHashMap(particleMap)
                particleMap.clear()
                /*println("emissione $snapshot")
                snapshot.values.forEach {
                    println(it)
                }*/
                snapshot
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    fun generateStream(
        flow: Flow<ParticleVisualizationModel>,
        samplingInterval: Int
    ) {
        animationDataStreamerUseCase.invoke(flow)

        animationFlow = transformStream(samplingInterval)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )
    }
}