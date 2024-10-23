package com.fraro.composable_realtime_animations.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.ParticleAnimationModel
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import com.fraro.composable_realtime_animations.data.models.ScreenPosition
import com.fraro.composable_realtime_animations.ui.viewmodels.RealtimeAnimationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun RealtimeAnimationCanvas(
    animationFlow: Flow<ParticleVisualizationModel>,
    samplingRate: Int,
    iconsBitmap: Map<Long, ImageBitmap>? = null,
    particlesSizes: Map<Long, Float>? = null
) {

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = context as ViewModelStoreOwner

    val viewModel: RealtimeAnimationViewModel = ViewModelProvider(lifecycleOwner)[RealtimeAnimationViewModel::class.java]

    val particlesAnimMap = remember {
        mutableStateMapOf<Long, ParticleAnimationModel>()
    }

    viewModel.generateStream(
        animationFlow,
        samplingRate
    )

    val collectedFlow = viewModel.animationFlow.collectAsStateWithLifecycle(
        initialValue = null,
        minActiveState = Lifecycle.State.RESUMED
    )

    collectedFlow.value?.let { map ->
        map.forEach { particle ->
            val found = particlesAnimMap[particle.key]
            if (found != null) {
                val currentScreenPosition = findStaticOrAnimatedCurrentScreenPosition(found)
                val nextScreenPosition = particle.value.screenPosition
                CoroutineScope(Dispatchers.Main).launch {
                    found.animatedOffset?.stop()
                    found.animatedHeading?.stop()
                }
                val animatedOffset = Animatable(currentScreenPosition.offset, Offset.VectorConverter)
                val animatedHeading = Animatable(currentScreenPosition.heading, Float.VectorConverter)

                particlesAnimMap[particle.key] = ParticleAnimationModel(
                    prev = currentScreenPosition,
                    next = nextScreenPosition,
                    animatedHeading = animatedHeading,
                    animatedOffset = animatedOffset,
                    bitmap = iconsBitmap?.get(particle.key),
                    duration = particle.value.duration
                )

                coroutineScope.launch {
                    val currParticle = particlesAnimMap[particle.key]
                    currParticle?.let {
                        animatedOffset.animateTo(
                            nextScreenPosition.offset,
                            tween(durationMillis = it.duration * 2, easing = LinearEasing)
                        )
                        animatedHeading.animateTo(
                            nextScreenPosition.heading,
                            tween(durationMillis = it.duration * 2, easing = LinearEasing)
                        )
                    }
                }
            }
            else {
                particlesAnimMap[particle.key] = ParticleAnimationModel(
                    prev = particle.value.screenPosition,
                    next = particle.value.screenPosition,
                    animatedHeading = null,
                    animatedOffset = null,
                    bitmap = null,
                    duration = particle.value.duration
                )
            }
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        particlesAnimMap.forEach {
            if (it.value.prev.offset.x >= 0 && it.value.prev.offset.y >= 0) {
                it.value.animatedOffset?.let { offset ->
                    val image = it.value.bitmap
                    if (image != null) {
                        rotate(
                            degrees = it.value.prev.heading,
                            pivot = Offset(
                                offset.value.x + image.width / 2,
                                offset.value.y + image.height / 2
                            )
                        ) {
                            drawImage(
                                image = image,
                                topLeft = offset.value
                            )
                        }
                    } else {
                        val rectWidth = particlesSizes?.get(it.key) ?: 20f
                        val rectHeight = particlesSizes?.get(it.key) ?: 20f
                        rotate(
                            degrees = it.value.prev.heading,
                            pivot = Offset(
                                offset.value.x + rectWidth / 2f,
                                offset.value.y + rectHeight / 2f
                            )
                        ) {
                            drawRect(
                                topLeft = Offset(
                                    x = offset.value.x,
                                    y = offset.value.y
                                ),
                                size = Size(
                                    width = rectWidth,
                                    height = rectHeight
                                ),
                                color = Color.Black.copy(alpha=0.3F),
                                style = Fill
                            )
                        }
                    }
                }
            }
        }
    }

}

fun findStaticOrAnimatedCurrentScreenPosition(found: ParticleAnimationModel): ScreenPosition {
    var currOffset = found.next.offset
    var currHeading = found.next.heading
    found.animatedOffset?.value?.let { currentAnimatedOffset ->
        currOffset = currentAnimatedOffset
    }
    found.animatedHeading?.value?.let { currentAnimatedHeading ->
        currHeading = currentAnimatedHeading
    }
    return ScreenPosition(currOffset, currHeading)
}
