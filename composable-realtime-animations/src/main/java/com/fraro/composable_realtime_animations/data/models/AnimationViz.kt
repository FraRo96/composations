package com.fraro.composable_realtime_animations.data.models

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline.Generic
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

enum class Animations {
    ROTATION, OFFSET, COLOR, SHAPE, SIZE
}

sealed interface Value<T> {
    data class Static<T>(val valueHolder: StaticValueHolder<T>) : Value<T>
    data class Animated<T> (val animation: Animation<T>) : Value<T>
    data object Stop : Value<Any>
}

class StaticValueHolder<T>(
    val value: T,
    private val vectorConverter: TwoWayConverter<T, AnimationVector>
) {
    fun animateFromStatic(duration: Int) : Animation<T> {
        return Animation(
            initialValue = value,
            duration = duration,
            animatable = Animatable(
                initialValue = value,
                typeConverter = vectorConverter
            )
        )
    }
}

open class Animation<T>(
    val initialValue: T,
    val duration: Int,
    val animatable: Animatable<T, AnimationVector>
) {

    fun getCurrentValue(): T {
        return animatable.value
    }
}

class MorphAnimation (
    initialValue: Float,
    duration: Int,
    animatable: Animatable<Float, AnimationVector>,
    val shape1: RoundedPolygon,
    val shape2: RoundedPolygon,
    val morph: Morph
) : Animation<Float>(
                        initialValue,
                        duration,
                        animatable
) {

    fun getCurrentMorphPath(): Path {
        return morph.toPath(progress = animatable.value).asComposePath()
    }
}