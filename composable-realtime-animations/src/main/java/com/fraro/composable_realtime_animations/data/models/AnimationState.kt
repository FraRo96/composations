package com.fraro.composable_realtime_animations.data.models

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.asComposePath
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

enum class AnimationType {
    ROTATION, OFFSET, COLOR, SHAPE, SIZE
}

class StateHolder<T,V: AnimationVector>(
    val id: Long,
    private val state: State<T,V>,
    val animationType: AnimationType,
    val wrappedStateHolders: List<StateHolder<*,*>>? = null,
) {

    fun getPartialState(): State<T,V> = state

    fun getState(): Map<AnimationType, State<*,*>> {
        val stateMap = mutableMapOf<AnimationType, State<*,*>>(animationType to state)
        wrappedStateHolders?.forEach { wrapped ->
           stateMap[wrapped.animationType] = wrapped.state
        }
        return stateMap.toMap()
    }
}

sealed interface State<T,V: AnimationVector> {
    data class Start<T,V: AnimationVector> (
        val visualDescriptor: VisualDescriptor<T,V>) : State<T,V>
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
        println("Nuova animazione da ${animatable.value} verso $targetValue durata $durationMillis")
        animatable.animateTo(
            targetValue = targetValue,
            animationSpec = this.animationSpec,
        )
    }
}

class MorphVisualDescriptor (
    currentValue: Float,
    targetValue: Float,
    durationMillis: Int,
    animationType: AnimationType,
    animatable: Animatable<Float, AnimationVector1D>,
    isAnimated: Boolean,
    animationSpec: AnimationSpec<Float>,
    var shape1: Shape,
    var shape2: Shape? = null
) : VisualDescriptor<Float, AnimationVector1D>(
                        currentValue,
                        targetValue,
                        durationMillis,
                        animationType,
                        animatable,
                        animationSpec,
                        isAnimated,
                        //vectorConverter
) {

    val morph: Morph? = setMorph(shape1, shape2)

    fun setMorph(shape1: Shape, shape2: Shape?): Morph? {
        val polygon1 = (shape1 as? Shape.CustomPolygonalShape)?.roundedPolygon
        val polygon2 = (shape2 as? Shape.CustomPolygonalShape)?.roundedPolygon
        return polygon1?.let {
            polygon2?.let {
                this.shape1 = shape1
                this.shape2 = shape2
                Morph(polygon1, polygon2)
            }
        }
    }

    fun getStaticOrAnimatedShape(): Shape =
        if (isAnimated)
            morph?.let {
                Shape.CustomPolygonalShape(
                    path = it.toPath(progress = animatable.value).asComposePath(),
                    //size = Size.RescaleFactor(100F)
                )
            } ?: shape1
        else shape1
}