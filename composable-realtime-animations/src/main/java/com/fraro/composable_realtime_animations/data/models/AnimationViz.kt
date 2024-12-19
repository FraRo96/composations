package com.fraro.composable_realtime_animations.data.models

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

data class ElementVisualization(
    val rotation: Value<Double>,
    val offset: Value<Offset>,
    val color: Value<Float>,
    val shape: Value<Shape>,
    val size: Value<Size>
)

sealed interface Value<out T> {
    data class Static<T>(val data: T) : Value<T>
    data class Animated<T, Animation>(val animation: Animation) : Value<T>
}

open class Animation<T, V : AnimationVector>(
    val initialValue: T,
    val duration: Int,
    val animatable: Animatable<T, V>
) {
    fun getCurrentValue(): T {
        return animatable.value
    }
}

class MorphAnimation (
    initialValue: Float,
    duration: Int,
    animatable: Animatable<Float, AnimationVector1D>,
    val shape1: RoundedPolygon,
    val shape2: RoundedPolygon,
    val morph: Morph
) : Animation<Float, AnimationVector1D>(initialValue, duration, animatable) {

    fun getCurrentMorphPath(): Path {
        return morph.toPath(progress = animatable.value).asComposePath()
    }
}