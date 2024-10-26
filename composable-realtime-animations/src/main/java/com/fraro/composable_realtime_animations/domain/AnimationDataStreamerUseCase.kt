package com.fraro.composable_realtime_animations.domain

import androidx.lifecycle.viewModelScope
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class AnimationDataStreamerUseCase {

    private val _streamFlow = MutableStateFlow<ParticleVisualizationModel?>(null)
    val streamFlow: StateFlow<ParticleVisualizationModel?> = _streamFlow

    operator fun invoke(flow: Flow<ParticleVisualizationModel>) {
        CoroutineScope(Dispatchers.Default + Job()).launch {
            flow.stateIn(
                scope = this,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            ).collect {
                _streamFlow.value = it
            }
        }
    }

}