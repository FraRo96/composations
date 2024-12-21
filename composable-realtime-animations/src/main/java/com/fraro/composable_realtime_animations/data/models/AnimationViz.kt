package com.fraro.composable_realtime_animations.data.models

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.ui.graphics.asComposePath
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath

enum class Animations {
    ROTATION, OFFSET, COLOR, SHAPE, SIZE
}

sealed interface State<T> {
    data class Start<T> (val animation: Animation<T>) : State<T>
    data class Existing<T> (val animation: Animation<T>) : State<T>
    data object Stop : State<Any>
    data object Forget: State<Any>
}

open class Animation<T>(
    var currentValue: T,
    //val duration: Int,
    var animatable: Animatable<T, AnimationVector>,
    var animationSpec: AnimationSpec<T>,
    var isAnimated: Boolean,
    //private val vectorConverter: TwoWayConverter<T, AnimationVector>
) {

    fun getCurrentValue(): T = if (isAnimated) animatable.value else currentValue

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

    suspend fun animateTo(targetValue: Any?) {
        val targetValueCast = targetValue as T
        animatable.animateTo(
            targetValue = targetValueCast,
            animationSpec = animationSpec,
        )
    }
}

class MorphAnimation (
    currentValue: Float,
    duration: Int,
    animatable: Animatable<Float, AnimationVector>,
    isAnimated: Boolean,
    animationSpec: AnimationSpec<Float>,
    //vectorConverter: TwoWayConverter<Float, AnimationVector>,
    val shape1: Shape,
    val shape2: Shape,
    val morph: Morph
) : Animation<Float>(
                        currentValue,
                        //duration,
                        animatable,
                        animationSpec,
                        isAnimated,
                        //vectorConverter
) {

    fun getCurrentShape(): Shape {
        return animatable?.let {
            Shape.CustomPolygonalShape(
                path = morph.toPath(progress = it.value).asComposePath(),
                size = Size.RescaleFactor(1F)
            )
        } ?: shape1
    }
}