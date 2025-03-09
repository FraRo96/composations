package com.fraro.composable_realtime_animations.data.models

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

enum class AnimationType {
    ROTATION, OFFSET
}

class StateHolder<T,V: AnimationVector>(
    private val state: State<T,V>,
    private val animationType: AnimationType,
    wrappedStateHolders: List<StateHolder<*,*>>? = null,
) {
    private val stateMap = mutableMapOf<AnimationType, State<*,*>>(animationType to state)

    init {
        wrappedStateHolders?.forEach { wrapped ->
            stateMap[wrapped.animationType] = wrapped.state
        }
    }

    fun getPartialState(): State<T,V> = state

    fun getState(): Map<AnimationType, State<*,*>> {
        return stateMap.toMap()
    }
}

sealed interface State<T,V: AnimationVector> {
    data class Start<T,V: AnimationVector> (
        val visualDescriptor: VisualDescriptor<T,V>
    ) : State<T,V>
    data class Animated<T> (val animation: Animation<T>) : State<T, AnimationVector>
    data class Fixed<T> (val targetValue: T) : State<T, AnimationVector>
    object Pause : State<Any, AnimationVector>
    data object Stop: State<Any, AnimationVector>
}

class Animation<T>(
    val animationSpec: AnimationSpec<T>,
    val durationMillis: Int,
    val targetValue: T
)

open class VisualDescriptor<T,V: AnimationVector>(
    var currentValue: T,
    var targetValue: T? = null,
    var durationMillis: Int,
    val animationType: AnimationType,
    var animatable: Animatable<T, V>,
    var animationSpec: AnimationSpec<T>,
    var isAnimated: Boolean
) {

    fun getStaticOrAnimatedValue(): T = if (isAnimated) animatable.value else currentValue

    fun stopAnimation() {
        isAnimated = false
        currentValue = animatable.value
    }

    suspend fun animateTo(targetValue: T) {
        animatable.stop()

        animatable.animateTo(
            targetValue = targetValue,
            animationSpec = animationSpec,
        )
    }

    suspend fun animateTo() {
        if (isAnimated) {
            targetValue?.let {
                animatable.animateTo(
                    targetValue = it,
                    animationSpec = animationSpec,
                )
            }
        }
    }

    suspend fun animateTo(
        targetValue: T,
        animationSpec: AnimationSpec<T>,
        durationMillis: Int
    ) {
        animatable.stop()
        this.animationSpec = animationSpec
        this.durationMillis = durationMillis
        animatable.animateTo(
            targetValue = targetValue,
            animationSpec = this.animationSpec,
        )
    }
}