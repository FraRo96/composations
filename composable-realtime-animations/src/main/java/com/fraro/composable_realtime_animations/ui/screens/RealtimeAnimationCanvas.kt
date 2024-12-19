package com.fraro.composable_realtime_animations.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.AbstractElement
import com.fraro.composable_realtime_animations.data.models.ElementVisualization
import com.fraro.composable_realtime_animations.data.models.Animation
import com.fraro.composable_realtime_animations.data.models.MorphAnimation
import com.fraro.composable_realtime_animations.data.models.ScreenPosition
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.composable_realtime_animations.data.models.Size
import com.fraro.composable_realtime_animations.data.models.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RealtimeAnimationCanvas(
    animationFlow: StateFlow<Map<Long, AbstractElement>>,
    samplingInterval: Int = 100,
    isStartedCallback: (() -> Unit)? = null
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

        val particlesAnimMap = remember {
            mutableMapOf<Long, ElementVisualization>()
        }

        val collectedFlow by animationFlow.collectAsStateWithLifecycle(
            initialValue = mapOf(),
            minActiveState = Lifecycle.State.RESUMED
        )

        LaunchedEffect(key1 = collectedFlow) {
            animateCanvas(
                collectedFlow,
                particlesAnimMap,
                coroutineScope,
                isStartedCallback
            )
        }

        val shapeA = remember {
            RoundedPolygon(
                12,
                rounding = CornerRounding(0.2f)
            )
        }
        val shapeB = remember {
            RoundedPolygon.star(
                12,
                rounding = CornerRounding(0.2f)
            )
        }
        val morph = remember {
            Morph(shapeA, shapeB)
        }
        val infiniteTransition = rememberInfiniteTransition("infinite outline movement")
        val animatedProgress = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "animatedMorphProgress"
        )

        LaunchedEffect(animatedProgress) {
            snackbarHostState.showSnackbar(
                "Sampling interval not valid.",
                "Change",
                duration = SnackbarDuration.Long
            )
        }

        val animatedRotation = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "animatedMorphProgress"
        )

        /*Box {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                collectedFlow?.let {
                    it.forEach { (key, value) ->
                        println("positions ${value.screenPosition.offset}")
                        drawCircle(
                            color = Color.Red.copy(alpha = 0.8f),
                            center = value.screenPosition.offset,
                            radius = 20.dp.toPx()
                        )
                    }
                }
            }
        }*/

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            fun canvasDrawRect(
                offset: Offset,
                heading: Float?,
                size: DoubleAxisMeasure?,
                color: Color?
            ) {
                val rectWidth = //size?.width ?:
                    100f
                val rectHeight = //size?.height ?:
                    100f
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
                        x = offset.x,
                        y = offset.y
                    )
                ) {
                    drawLine(
                        start = Offset(
                            x = offset.x - length / 2,
                            y = offset.y
                        ),
                        end = Offset(
                            x = offset.x + length / 2,
                            y = offset.y
                        ),
                        strokeWidth = 10F,
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
                        x = offset.x,
                        y = offset.y
                    )
                ) {
                    drawOval(
                        topLeft = Offset(
                            x = offset.x - width / 2,
                            y = offset.y - height / 2,
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
                    centerX = offset.x,
                    centerY = offset.y
                )
                val path = regPolygon.toPath().asComposePath()
                rotate(
                    degrees = heading ?: 0F,
                    pivot = Offset(
                        x = offset.x,
                        y = offset.y
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
                val scale = size?.scale ?: 100F
                val pathBounds = path.getBounds()
                val pivotX = (pathBounds.topLeft.x + pathBounds.bottomRight.x) / 2F
                val pivotY = (pathBounds.topLeft.y + pathBounds.bottomRight.y) / 2F

                rotate(
                    degrees = heading ?: 0F,
                    pivot = Offset(
                        x = offset.x,
                        y = offset.y
                    )
                ) {
                    scale(
                        scale = scale,
                        pivot = Offset(offset.x + pivotX, offset.y + pivotY)
                    ) {
                        translate(
                            left = offset.x + pivotX,
                            top = offset.y + pivotY
                        ) {
                            drawPath(
                                path = CustomRotatingMorphShape(
                                    morph,
                                    animatedProgress.value,
                                    animatedRotation.value
                                ).createPath(
                                    Size(50f,50f),
                                    offset.x,
                                    offset.y
                                ), //path,
                                color = color ?: Color.Black.copy(alpha = 0.3F)
                            )
                        }
                    }
                }
            }

            particlesAnimMap.values.forEach { particle ->
                val shape = findShape(particle.shape)
                val color = findValue(particle.color)
                val size = findValue(particle.size)
                val offset = findValue(particle.offset)
                val rotation = findValue(particle.rotation)

                when (shape) {
                    is Shape.Segment -> {

                    }
                    is Shape.Ellipse -> {

                    }
                    is Shape.Rectangle -> {

                    }
                    is Shape.RegularPolygon -> {

                    }
                    is Shape.CustomPolygonalShape -> {
                        canvasDrawCustomPolygonalShape(
                            offset = offset,
                            heading = rotation.toFloat(),
                            path = shape.path,
                            size = size as Size.RescaleFactor,
                            color = Color.Red
                        )
                    }

                    Shape.Unspecified -> {

                    }
                }
                val path = ((particle.shape as Value.Animated<*,*>).animation as MorphAnimation).getCurrentMorphPath()

                }
                    entry.animatedOffset?.value?.let { offset ->
                        with(entry.animatedOrbiting?.value) heading@ {

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

fun findShape(shape: Value<Shape>): Shape {
    when (shape) {
        is Value.Static -> {
            return shape.data
        }
        is Value.Animated<*, *> -> {
            val path = ((shape as Value.Animated<*,*>).animation as MorphAnimation).getCurrentMorphPath()
            return Shape.CustomPolygonalShape(path)
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> findValue(value: Value<T>): T {
    return when (value) {
        is Value.Static -> {
            value.data
        }
        is Value.Animated<*, *> -> {
            ((value.animation as Animation<*, *>).getCurrentValue()) as T
        }
    }
}



fun animateCanvas(
    map: Map<Long, AbstractElement>,
    particlesAnimMap: MutableMap<Long, Animation>,
    coroutineScope: CoroutineScope,
    isStartedCallback: (() -> Unit)?
) {
    CoroutineScope(Dispatchers.Default).launch {
        map.forEach { particle ->
            //println("posizione# ${particle.value.screenPosition}")
            val foundAnimation = particlesAnimMap[particle.key]
            foundAnimation?.let { found ->
                val currentScreenPosition = findStaticOrAnimatedCurrentScreenPosition(found)
                val nextUnscaledScreenPosition = particle.value.screenPosition
                //println("pos corrente ${currentScreenPosition.offset}")
                //println("pos prossima ${nextScreenPosition.offset}")
                val nextScreenPosition = ScreenPosition(
                    offset = nextUnscaledScreenPosition.offset
                            + (nextUnscaledScreenPosition.offset
                            - currentScreenPosition.offset) * particle.value.delayFactor,
                    heading = nextUnscaledScreenPosition.heading
                            + nextUnscaledScreenPosition.heading * particle.value.delayFactor
                )
                coroutineScope.launch {
                    found.animatedOffset?.stop()
                    found.animatedHeading?.stop()
                }
                val animatedOffset = Animatable(currentScreenPosition.offset, Offset.VectorConverter)
                val animatedHeading = Animatable(currentScreenPosition.heading, Float.VectorConverter)

                particlesAnimMap[particle.key] = DelayedAnimation(
                    prev = currentScreenPosition,
                    next = nextScreenPosition,
                    animatedHeading = animatedHeading,
                    animatedOffset = animatedOffset,
                    animationElement = found.animationElement,
                    duration = particle.value.duration + (particle.value.duration * particle.value.delayFactor).roundToInt()
                )

                val currParticle = particlesAnimMap[particle.key]
                currParticle?.let {
                    coroutineScope.launch {
                        animatedOffset.animateTo(
                            nextScreenPosition.offset,
                            tween(durationMillis = it.duration, easing = LinearEasing)
                        )
                    }
                   coroutineScope.launch {
                        animatedHeading.animateTo(
                            nextScreenPosition.heading,
                            tween(durationMillis = it.duration, easing = LinearEasing)
                        )
                    }
                }
            }
            if (foundAnimation == null) {
                println("pos prima ${particle.value.screenPosition}")
                particlesAnimMap[particle.key] = DelayedAnimation(
                    prev = particle.value.screenPosition,
                    next = particle.value.screenPosition,
                    animatedHeading = null,
                    animatedOffset = null,
                    animationElement = particle.value,
                    duration = particle.value.duration
                )
                isStartedCallback?.invoke()
            }
        }
    }
}

fun findStaticOrAnimatedCurrentScreenPosition(found: DelayedAnimation): ScreenPosition {
    val currOffset = found.animatedOffset?.value ?: found.next.offset
    val currHeading = found.animatedHeading?.value ?: found.next.heading
    println("offset: $currOffset")
    return ScreenPosition(currOffset, currHeading)
}

fun angleFromUnitVector(x: Float, y: Float): Float {
    val angleRadians = atan2(y, x)
    val angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat()
    return if (angleDegrees < 0) angleDegrees + 360 else angleDegrees
}

@OptIn(FlowPreview::class)
fun Flow<AbstractElement>.toBatchedStateFlow(
    samplingInterval: Long = 50L
): StateFlow<Map<Long, AbstractElement>> {
    val sharedMap = mutableMapOf<Long, AbstractElement>() // Shared map for the latest particles

    return this
        .onEach { particle ->
            // Add the particle to the shared map
            sharedMap[particle.id] = particle
        }
        .sample(samplingInterval) // Emit updates at the specified interval
        .map {
            // Create a snapshot of the map to emit
            val snapshot = sharedMap.toMap()
            sharedMap.clear()

            snapshot
        }
        .distinctUntilChanged()
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(),
            initialValue = mapOf()
        )
}

class CustomRotatingMorphShape(
    private val morph: Morph,
    private val percentage: Float,
    private val rotation: Float
) : androidx.compose.ui.graphics.Shape {

    private val matrix = Matrix()
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        // Below assumes that you haven't changed the default radius of 1f, nor the centerX and centerY of 0f
        // By default this stretches the path to the size of the container, if you don't want stretching, use the same size.width for both x and y.
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        matrix.rotateZ(rotation)

        val path = morph.toPath(progress = percentage).asComposePath()
        path.transform(matrix)

        return Outline.Generic(path)
    }

    fun createPath(
        size: Size,
        translateX: Float,
        translateY: Float
    ): Path {
        // Below assumes that you haven't changed the default radius of 1f, nor the centerX and centerY of 0f
        // By default this stretches the path to the size of the container, if you don't want stretching, use the same size.width for both x and y.
        //matrix.scale(size.width / 2f, size.height / 2f)
        //matrix.translate(translateX, translateY)
        //matrix.rotateZ(rotation)

        val path = morph.toPath(progress = percentage).asComposePath()
        //path.transform(matrix)

        return path
    }
}



