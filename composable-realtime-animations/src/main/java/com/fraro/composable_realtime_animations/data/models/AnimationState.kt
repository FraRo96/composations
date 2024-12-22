package com.fraro.composable_realtime_animations.data.models

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.asComposePath
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class AnimationType {
    ROTATION, OFFSET, COLOR, SHAPE, SIZE
}

class StateHolder<T,V: AnimationVector>(
    val id: Long,
    private val state: State<T,V>,
    val animationType: AnimationType,
    private val wrappedStateHolders: List<StateHolder<*,*>>? = null,
) {

    fun getPartialState(): State<*,*> = state

    fun getState(): Map<AnimationType, State<*,*>> {
        val stateMap = mutableMapOf<AnimationType, State<*,*>>(animationType to state)
        wrappedStateHolders?.forEach { holder ->
            CoroutineScope(Dispatchers.Default).launch {
               stateMap[holder.animationType] = holder.state
            }
        }
        return stateMap.toMap()
    }
}

sealed interface State<T,V: AnimationVector> {
    data class Start<T,V: AnimationVector> (
        val visualDescriptor: VisualDescriptor<T,V>) : State<T,V>
    data class Animated<T> (val animation: Animation<T>) : State<T, AnimationVector>
    data class Fixed<T> (val targetValue: T) : State<T, AnimationVector>
    data object Stop : State<Any, AnimationVector>
    data object Forget: State<Any, AnimationVector>
}

class Animation<T>(
    val animationSpec: AnimationSpec<T>,
    val durationMillis: Int,
    val targetValue: T
)


open class VisualDescriptor<T,V: AnimationVector>(
    var currentValue: T,
    var durationMillis: Int,
    val animationType: AnimationType,
    var animatable: Animatable<T, V>,
    var animationSpec: AnimationSpec<T>,
    var isAnimated: Boolean,
) {

    fun getStaticOrAnimatedValue(): T = if (isAnimated) animatable.value else currentValue

    fun startAnimation() {
        isAnimated = true
        animatable = Animatable(
            initialValue = currentValue,
            typeConverter = animatable.typeConverter
        )
    }

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

class MorphVisualDescriptor (
    currentValue: Float,
    durationMillis: Int,
    animationType: AnimationType,
    animatable: Animatable<Float, AnimationVector1D>,
    isAnimated: Boolean,
    animationSpec: AnimationSpec<Float>,
    val shape1: Shape,
    val shape2: Shape,
    val morph: Morph
) : VisualDescriptor<Float, AnimationVector1D>(
                        currentValue,
                        durationMillis,
                        animationType,
                        animatable,
                        animationSpec,
                        isAnimated,
                        //vectorConverter
) {

    fun getStaticOrAnimatedShape(): Shape =
        if (isAnimated)
            Shape.CustomPolygonalShape(
                path = morph.toPath(progress = animatable.value).asComposePath(),
                size = Size.RescaleFactor(1F)
            )
        else shape1
}