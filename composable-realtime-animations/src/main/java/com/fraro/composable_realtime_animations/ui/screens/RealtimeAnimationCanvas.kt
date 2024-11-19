package com.fraro.composable_realtime_animations.ui.screens

import android.annotation.SuppressLint
import android.graphics.PointF
import com.fraro.composable_realtime_animations.data.models.Size.DoubleAxisMeasure
import com.fraro.composable_realtime_animations.data.models.Size.SingleAxisMeasure
import com.fraro.composable_realtime_animations.data.models.Size.RescaleFactor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.plus
import androidx.core.graphics.times
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.ParticleAnimationModel
import com.fraro.composable_realtime_animations.data.models.ParticleVisualizationModel
import com.fraro.composable_realtime_animations.data.models.ScreenPosition
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.composable_realtime_animations.ui.viewmodels.RealtimeAnimationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RealtimeAnimationCanvas(
    animationFlow: Flow<ParticleVisualizationModel>,
    samplingInterval: Int
) {

    val snackbarHostState = remember { SnackbarHostState() }

    val configuration = LocalConfiguration.current

    if (samplingInterval <= 0 || samplingInterval >= 10000000) {

        LaunchedEffect(samplingInterval) {
            snackbarHostState.showSnackbar(
                "Sampling interval not valid.",
                "Change",
                duration = SnackbarDuration.Long
            )
        }
        Scaffold(
            modifier = Modifier.padding(bottom = 100.dp),
            // attach snackbar host state to the scaffold
            scaffoldState = rememberScaffoldState(snackbarHostState = snackbarHostState),
            content = {} // content is not mandatory
        )
    }

    else {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val lifecycleOwner = context as ViewModelStoreOwner

        val viewModel: RealtimeAnimationViewModel = ViewModelProvider(lifecycleOwner)[RealtimeAnimationViewModel::class.java]

        val particlesAnimMap = remember {
            mutableStateMapOf<Long, ParticleAnimationModel>()
        }

        viewModel.generateStream(
            animationFlow,
            samplingInterval
        )

        val collectedFlow = viewModel.animationFlow.collectAsStateWithLifecycle(
            initialValue = null,
            minActiveState = Lifecycle.State.RESUMED
        )

        collectedFlow.value?.let { map ->
            map.forEach { particle ->
                val foundAnimation = particlesAnimMap[particle.key]
                foundAnimation?.let { found ->
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
                        particleVisualizationModel = found.particleVisualizationModel,
                        duration = particle.value.duration
                    )

                    val currParticle = particlesAnimMap[particle.key]
                    currParticle?.let {
                        coroutineScope.launch {
                            animatedOffset.animateTo(
                                nextScreenPosition.offset,
                                tween(durationMillis = it.duration * 2, easing = LinearEasing)
                            )
                        }
                        coroutineScope.launch {
                            animatedHeading.animateTo(
                                nextScreenPosition.heading,
                                tween(durationMillis = it.duration * 2, easing = LinearEasing)
                            )
                        }
                    }
                }
                if (foundAnimation == null) {
                    particlesAnimMap[particle.key] = ParticleAnimationModel(
                        prev = particle.value.screenPosition,
                        next = particle.value.screenPosition,
                        animatedHeading = null,
                        animatedOffset = null,
                        particleVisualizationModel = particle.value,
                        duration = particle.value.duration
                    )
                }
            }
        }


        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            fun canvasDrawImage(
                offset: Offset,
                heading: Float?,
                bitmap: ImageBitmap
            ) {
                rotate(
                    degrees = heading ?: 0F,
                    pivot = Offset(
                        x = offset.x + bitmap.width / 2,
                        y = offset.y + bitmap.height / 2
                    )
                ) {
                    drawImage(
                        image = bitmap,
                        topLeft = offset
                    )
                }
            }

            fun canvasDrawRect(
                offset: Offset,
                heading: Float?,
                size: DoubleAxisMeasure?,
                color: Color?
            ) {
                val rectWidth = size?.width ?: 20f
                val rectHeight = size?.height ?: 20f
                rotate(
                    degrees = heading ?: 0F,
                    pivot = Offset(
                        x = offset.x + rectWidth / 2F,
                        y = offset.y + rectHeight / 2F
                    )
                ) {
                    drawRect(
                        topLeft = Offset(
                            x = offset.x,
                            y = offset.y
                        ),
                        size = Size(
                            width = rectWidth,
                            height = rectHeight
                        ),
                        color = color ?: Color.Black.copy(alpha=0.3F),
                        style = Fill
                    )
                }
            }

            fun canvasDrawLine(
                offset: Offset,
                heading: Float?,
                size: SingleAxisMeasure?,
                color: Color?
            ) {
                val length = size?.size ?: 40F
                rotate(
                    degrees = heading ?: 0F,
                    pivot = Offset(
                        x = offset.x + length / 2F,
                        y = offset.y
                    )
                ) {
                    drawLine(
                        start = Offset(
                            x = offset.x,
                            y = offset.y
                        ),
                        end = Offset(
                            x = offset.x + length,
                            y = offset.y
                        ),
                        color = color ?: Color.Black.copy(alpha=0.3F),
                    )
                }
            }

            fun canvasDrawOval(
                offset: Offset,
                heading: Float?,
                size: DoubleAxisMeasure?,
                color: Color?
            ) {
                val width = size?.width ?: 20f
                val height = size?.height ?: 20f
                rotate(
                    degrees = heading ?: 0F,
                    pivot = Offset(
                        x = offset.x + width / 2F,
                        y = offset.y + height / 2F
                    )
                ) {
                    drawOval(
                        topLeft = Offset(
                            x = offset.x,
                            y = offset.y
                        ),
                        size = Size(
                            width = width,
                            height = height
                        ),
                        color = color ?: Color.Black.copy(alpha=0.3F),
                        style = Fill
                    )
                }
            }

            fun canvasDrawPolygon(
                offset: Offset,
                heading: Float?,
                nVertices: Int,
                size: SingleAxisMeasure?,
                color: Color?
            ) {

                val radius = size?.size ?: 40F
                val regPolygon = RoundedPolygon(
                    numVertices = nVertices,
                    radius = radius,
                    centerX = radius / 2,
                    centerY = radius / 2
                )
                val path = regPolygon.toPath().asComposePath()
                rotate(
                    degrees = heading ?: 0F,
                    pivot = Offset(
                        x = offset.x + radius / 2F,
                        y = offset.y + radius / 2F
                    )
                ) {
                    drawPath(
                        path = path,
                        color = color ?: Color.Black.copy(alpha = 0.3F)
                    )
                }
            }

            fun canvasDrawCustomPolygonalShape(
                offset: Offset,
                path: Path,
                heading: Float?,
                size: RescaleFactor?,
                color: Color?
            ) {
                val pathBounds = path.getBounds()
                val pivotX = (pathBounds.topLeft.x + pathBounds.bottomRight.x) / 2F
                val pivotY = (pathBounds.topLeft.y + pathBounds.bottomRight.y) / 2F

                rotate(
                    degrees = heading ?: 0F,
                    pivot = Offset(
                        x = offset.x + pivotX,
                        y = offset.y + pivotY
                    )
                ) {
                    //         Move our path to the new position
                    translate(
                        left = offset.x + pivotX,
                        top = offset.y + pivotY
                    ) {
                        scale(
                            scale = size?.scale ?: 100F,
                            pivot = Offset(pivotX, pivotY)
                        ) {
                            drawPath(
                                path = path,
                                color = color ?: Color.Black.copy(alpha = 0.3F)
                            )
                        }
                    }
                }
            }

            particlesAnimMap.forEach { entry ->

                val isInScreenRange = entry.value.animatedOffset?.value?.let {
                    it.x >= 0 && it.x <= configuration.screenWidthDp.toFloat() &&
                    it.y >= 0 && it.y <= configuration.screenHeightDp.toFloat()
                } ?: false

                if (isInScreenRange) {

                    with(entry.value.particleVisualizationModel) particle@ {
                        entry.value.animatedOffset?.value?.let { offset ->
                            with(entry.value.animatedHeading?.value) heading@ {
                                entry.value.particleVisualizationModel.bitmap?.let { bitmap ->

                                    canvasDrawImage(
                                        offset = offset,
                                        heading = this@heading,
                                        bitmap = bitmap
                                    )
                                }

                                when (this@particle.shape) {

                                    is Shape.Segment -> {

                                        canvasDrawLine(
                                            offset = offset,
                                            heading = this@heading,
                                            size = this@particle.shape.size,
                                            color = this@particle.color
                                        )
                                    }

                                    is Shape.Rectangle -> {

                                        canvasDrawRect(
                                            offset = offset,
                                            heading = this@heading,
                                            size = this@particle.shape.size,
                                            color = this@particle.color
                                        )
                                    }

                                    is Shape.RegularPolygon -> {

                                        canvasDrawPolygon(
                                            offset = offset,
                                            heading = this@heading,
                                            nVertices = this@particle.shape.nVertices,
                                            size = this@particle.shape.size,
                                            color = this@particle.color
                                        )
                                    }

                                    is Shape.Ellipse -> {
                                        canvasDrawOval(
                                            offset = offset,
                                            heading = this@heading,
                                            size = this@particle.shape.size,
                                            color = this@particle.color
                                        )
                                    }

                                    is Shape.CustomPolygonalShape -> {

                                        canvasDrawCustomPolygonalShape(
                                            offset = offset,
                                            heading = this@heading,
                                            path = this@particle.shape.path,
                                            size = this@particle.shape.size,
                                            color = this@particle.color
                                        )
                                    }

                                    is Shape.Unspecified -> {
                                        canvasDrawRect(
                                            offset = offset,
                                            heading = this@heading,
                                            size = DoubleAxisMeasure(20F, 20F),
                                            color = this@particle.color
                                        )
                                    }
                                }
                            }
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
