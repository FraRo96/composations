package com.fraro.composable_realtime_animations.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.Animations
import com.fraro.composable_realtime_animations.data.models.DelayedElementDecorator
import com.fraro.composable_realtime_animations.data.models.Element
import com.fraro.composable_realtime_animations.data.models.MorphVisualDescriptor
import com.fraro.composable_realtime_animations.data.models.ScreenPosition
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.composable_realtime_animations.data.models.Size
import com.fraro.composable_realtime_animations.data.models.State
import com.fraro.composable_realtime_animations.data.models.State.*
import com.fraro.composable_realtime_animations.data.models.VectorElementDecorator
import com.fraro.composable_realtime_animations.data.models.VisualDescriptor
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

import androidx.compose.ui.geometry.Size as ComposeSize

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RealtimeAnimationCanvas(
    animationFlow: StateFlow<Map<Long, Element<*>>>,
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

        val elements = remember {
            mutableMapOf<Long, MutableMap<Int, VisualDescriptor<*>>>()
        }

        val collectedFlow by animationFlow.collectAsStateWithLifecycle(
            initialValue = mapOf(),
            minActiveState = Lifecycle.State.RESUMED
        )

        //LaunchedEffect(key1 = collectedFlow) {
        animateCanvas(
            collectedFlow,
            elements,
            coroutineScope,
            isStartedCallback
        )
        //}

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

            //TODO: animate blendMode, alpha and other properties

            fun canvasDrawRect(
                offset: Offset,
                heading: Float?,
                size: Size.DoubleAxisMeasure?,
                color: Color?
            ) {
                val rectWidth = size?.width ?:
                    100f
                val rectHeight = size?.height ?:
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
                        size = ComposeSize(
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
                size: Size.DoubleAxisMeasure?,
                color: Color?
            ) {
                val length = size?.height ?: 40F
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
                size: Size.DoubleAxisMeasure?,
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
                        size = ComposeSize(
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
                size: Size.SingleAxisMeasure?,
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
                size: Size.RescaleFactor?,
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
                                path = path,
                                color = color ?: Color.Black.copy(alpha = 0.3F)
                            )
                        }
                    }
                }
            }

            elements.values.forEach { element ->

                val rotation = element[Animations.ROTATION.ordinal]
                    ?.getCurrentValue() as Float
                val shape = (element[Animations.SHAPE.ordinal] as MorphVisualDescriptor)
                    .getCurrentShape()
                val color = element[Animations.COLOR.ordinal]
                    ?.getCurrentValue() as Color
                val size = element[Animations.SIZE.ordinal]
                    ?.getCurrentValue() as Size
                val offset = element[Animations.OFFSET.ordinal]
                    ?.getCurrentValue() as Offset

                when (shape) {
                    is Shape.Segment -> {
                        canvasDrawLine(
                            offset = offset,
                            heading = rotation,
                            size = size as Size.DoubleAxisMeasure,
                            color = color
                        )
                    }
                    is Shape.Ellipse -> {
                        canvasDrawOval(
                            offset = offset,
                            heading = rotation.toFloat(),
                            size = size as Size.DoubleAxisMeasure,
                            color = color
                        )
                    }
                    is Shape.Rectangle -> {
                        canvasDrawRect(
                            offset = offset,
                            heading = rotation.toFloat(),
                            size = size as Size.DoubleAxisMeasure,
                            color = color
                        )
                    }
                    is Shape.RegularPolygon -> {
                        canvasDrawPolygon(
                            offset = offset,
                            heading = rotation.toFloat(),
                            nVertices = shape.nVertices,
                            size = size as Size.SingleAxisMeasure,
                            color = color
                        )
                    }
                    is Shape.CustomPolygonalShape -> {
                        canvasDrawCustomPolygonalShape(
                            offset = offset,
                            heading = rotation.toFloat(),
                            path = shape.path,
                            size = size as Size.RescaleFactor,
                            color = color
                        )
                    }

                    Shape.Unspecified -> {

                    }
                }
            }
        }
    }
}

suspend fun <T,V> animateDescriptor(
    descriptors: MutableMap<Int, VisualDescriptor<*>>,
    index: Int,
    descriptor: VisualDescriptor<T>?,
    visualUpdate: State<out V>
) {

    /* same map index, same type */
    val visualUpdateCast = visualUpdate as State<out T>

    when (visualUpdateCast) {
        is Stop -> {
            descriptor?.stopAnimation()
        }

        is Fixed -> {
            descriptor?.animateTo(visualUpdateCast.targetValue)
        }

        is Animated -> {
            descriptor?.animateTo(
                visualUpdateCast.animation.targetValue,
                visualUpdateCast.animation.animationSpec as AnimationSpec<T>
            )
        }

        is Forget -> {
            descriptors.remove(index)
        }

        is Start -> {
            descriptors[index] = visualUpdateCast.visualDescriptor
        }
    }
}

fun animateCanvas(
    flow: Map<Long, Element<*>>,
    elements: MutableMap<Long, MutableMap<Int, VisualDescriptor<*>>>,
    coroutineScope: CoroutineScope,
    isStartedCallback: (() -> Unit)?
) {
    CoroutineScope(Dispatchers.Default).launch {
        flow.forEach { elementData ->
            when (elementData.value) {
                is DelayedElementDecorator<*> -> {
                    elements[elementData.key]?.let { descriptors ->
                        elementData.value.getData()
                            .values.forEachIndexed { index, visualUpdate ->
                                coroutineScope.launch {
                                    animateDescriptor(
                                        descriptors,
                                        index,
                                        descriptors[index],
                                        visualUpdate
                                    )
                                }
                            }
                    }
                }
                is VectorElementDecorator<*> -> {

                }
            }
        }
    }
}

fun angleFromUnitVector(x: Float, y: Float): Float {
    val angleRadians = atan2(y, x)
    val angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat()
    return if (angleDegrees < 0) angleDegrees + 360 else angleDegrees
}

@OptIn(FlowPreview::class)
fun Flow<Element<*>>.toBatchedStateFlow(
    samplingInterval: Long = 50L
): StateFlow<Map<Long, Element<*>>> {
    val sharedMap = mutableMapOf<Long, Element<*>>() // Shared map for the latest particles

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