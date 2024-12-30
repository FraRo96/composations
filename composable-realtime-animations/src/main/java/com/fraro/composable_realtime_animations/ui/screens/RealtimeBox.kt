package com.fraro.composable_realtime_animations.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.AnimationType
import com.fraro.composable_realtime_animations.data.models.State
import com.fraro.composable_realtime_animations.data.models.State.*
import com.fraro.composable_realtime_animations.data.models.StateHolder
import com.fraro.composable_realtime_animations.data.models.VisualDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

val randomColor
    get() = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

@Composable
fun RealtimeBox(
    animationFlow: StateFlow<StateHolder<*, *>?>,
    initialOffset: Offset,
    isStartedCallback: (() -> Unit)? = null,
    isStoppedCallback: (() -> Unit)? = null,
    composable: @Composable (() -> Unit)
) {

    val coroutineScope = rememberCoroutineScope()

    val multiVariableMap = remember {
        mutableMapOf<AnimationType, VisualDescriptor<*,*>>()
    }

    val collectedFlow by animationFlow.collectAsStateWithLifecycle(
        initialValue = null,
        minActiveState = Lifecycle.State.RESUMED
    )

    LaunchedEffect(key1 = collectedFlow) {
        collectedFlow?.let {
            animateMultiVariable(
                it,
                multiVariableMap,
                coroutineScope,
                isStartedCallback,
                isStoppedCallback
            )
        }
    }

    var offset = remember { initialOffset }
    var rotation = remember { 0f }

    multiVariableMap[AnimationType.OFFSET]?.let {
        offset = it.getStaticOrAnimatedValue() as Offset
    }

    multiVariableMap[AnimationType.ROTATION]?.let {
        rotation = it.getStaticOrAnimatedValue() as Float
    }

    AnimatedBox(offset, rotation, composable)
}

@Composable
fun AnimatedBox(
    offset: Offset,
    rotation: Float,
    function: @Composable () -> Unit
) {
    SideEffect { println("Animated box") }
    Box(modifier = Modifier
        .offset {
            IntOffset(
                offset.x.roundToInt(),
                offset.y.roundToInt()
            )
        }
        .rotate(rotation)
    ) {
        function()
    }
}

suspend fun <T,V> animateSingleVariable(
    descriptors: MutableMap<AnimationType, VisualDescriptor<*,*>>,
    descriptor: VisualDescriptor<T,*>?,
    visualUpdate: State<out V,*>,
    isStartedCallback: (() -> Unit)?,
    isStoppedCallback: (() -> Unit)?,
) {

    /* same map index, same type */
    val visualUpdateCast = visualUpdate as State<out T,*>

    when (visualUpdateCast) {
        is Pause -> {
            descriptor?.stopAnimation()
        }

        is Fixed -> {
            descriptor?.animateTo(
                targetValue = visualUpdateCast.targetValue
            )
        }

        is Animated -> {
            descriptor?.animateTo(
                durationMillis = visualUpdateCast.animation.durationMillis,
                targetValue = visualUpdateCast.animation.targetValue,
                animationSpec = visualUpdateCast.animation.animationSpec as AnimationSpec<T>
            )
        }

        is Stop -> {
            descriptor?.let {
                descriptors.remove(it.animationType)
            }
            isStoppedCallback?.invoke()
        }

        is Start -> {
            descriptors[visualUpdateCast.visualDescriptor.animationType] = visualUpdateCast.visualDescriptor
            isStartedCallback?.invoke()
        }
    }
}

fun animateMultiVariable(
    state: StateHolder<*, *>,
    descriptors: MutableMap<AnimationType, VisualDescriptor<*, *>>,
    coroutineScope: CoroutineScope,
    isStartedCallback: (() -> Unit)?,
    isStoppedCallback: (() -> Unit)?
) {

    val partialState = state.getPartialState()
    val states = state.getState()

    if (partialState is Start<*,*>) {
        isStartedCallback?.invoke()
        states.values.forEach { initialState ->
            if (initialState is Start<*,*>) {
                descriptors[initialState.visualDescriptor.animationType] =
                    initialState.visualDescriptor
                coroutineScope.launch {
                    initialState.visualDescriptor.animateTo()
                }
            }
        }
    } else {
        states.forEach { animType, visualUpdate ->
            val descriptor = descriptors[animType]
            descriptor?.let {
                coroutineScope.launch {
                    animateSingleVariable(
                        descriptors,
                        it,
                        visualUpdate,
                        isStartedCallback,
                        isStoppedCallback
                    )
                }
            }
        }
    }
}

@OptIn(FlowPreview::class)
fun Flow<StateHolder<*,*>>.toBatchedStateFlow(
    samplingInterval: Long = 50L
): StateFlow<StateHolder<*,*>?> {
    return this
        .sample(samplingInterval)
        .distinctUntilChanged()
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )
}