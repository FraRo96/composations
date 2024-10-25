package com.fraro.composable_realtime_animations.data.models

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap

data class ParticleAnimationModel(
    val prev: ScreenPosition,
    val next: ScreenPosition,
    val animatedHeading: Animatable<Float, AnimationVector1D>?,
    val animatedOffset: Animatable<Offset, AnimationVector2D>?,
    val particleVisualizationModel: ParticleVisualizationModel,
    val duration: Int
)