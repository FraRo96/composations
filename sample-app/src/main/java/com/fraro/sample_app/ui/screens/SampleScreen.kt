package com.fraro.sample_app.ui.screens

import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.plus
import androidx.core.graphics.times
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.Animation
import com.fraro.composable_realtime_animations.data.models.AnimationType
import com.fraro.composable_realtime_animations.data.models.MorphVisualDescriptor
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.composable_realtime_animations.data.models.State
import com.fraro.composable_realtime_animations.data.models.State.Start
import com.fraro.composable_realtime_animations.data.models.StateHolder
import com.fraro.composable_realtime_animations.data.models.VisualDescriptor
import com.fraro.composable_realtime_animations.ui.screens.RealtimeAnimationCanvas
import com.fraro.sample_app.ui.viewmodels.SampleViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SampleScreen() {
    val context = LocalContext.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: SampleViewModel = ViewModelProvider(lifecycleOwner)[SampleViewModel::class.java]

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

    var identifier = remember { 0L }
    val rounding = remember {
        val roundingNormal = 0.6f
        val roundingNone = 0f
        listOf(
            CornerRounding(roundingNormal),
            CornerRounding(roundingNone),
            CornerRounding(roundingNormal),
            CornerRounding(roundingNormal),
            CornerRounding(roundingNone),
            CornerRounding(roundingNormal),
        )
    }

    val vertices = remember {
        val radius = 1f
        val radiusSides = 0.8f
        val innerRadius = .1f
        floatArrayOf(
            radialToCartesian(radiusSides, 0f.toRadians()).x,
            radialToCartesian(radiusSides, 0f.toRadians()).y,
            radialToCartesian(radius, 90f.toRadians()).x,
            radialToCartesian(radius, 90f.toRadians()).y,
            radialToCartesian(radiusSides, 180f.toRadians()).x,
            radialToCartesian(radiusSides, 180f.toRadians()).y,
            radialToCartesian(radius, 250f.toRadians()).x,
            radialToCartesian(radius, 250f.toRadians()).y,
            radialToCartesian(innerRadius, 270f.toRadians()).x,
            radialToCartesian(innerRadius, 270f.toRadians()).y,
            radialToCartesian(radius, 290f.toRadians()).x,
            radialToCartesian(radius, 290f.toRadians()).y,
        )
    }

    val polygon = remember(vertices, rounding) {
        RoundedPolygon(
            vertices = vertices,
            perVertexRounding = rounding
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, end = 30.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row {
            Timer(viewModel.dragTimer, Color.Red)
        }
        Row {
            Timer(viewModel.animationTimer, Color.Blue)
        }
    }

    RealtimeAnimationCanvas(
        animationFlow = viewModel.animationEmitter.getTransformedFlow(),
        samplingInterval = 1000,
        isStartedCallback = {
            viewModel.animationTimer.startTimer(); }
    )

    var isShown by remember { mutableStateOf(true) }

    var prevOffset by remember {
        mutableStateOf(Offset(0f,0f))
    }

    if (isShown) {
        Box(modifier = Modifier.fillMaxSize()) {
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            var prevTime = 0L
            var deltaSum = 0
            var isFirstTime = true

            val _dragTrajectory: MutableList<StateHolder<*, *>> = remember { mutableListOf() }
            var dragTrajectory: List<StateHolder<*, *>>
            Box(
                Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .drawWithCache {
                        val roundedPolygonPath = polygon.toPath().asComposePath()
                        onDrawBehind {
                            scale(size.width * 0.5f, size.width * 0.5f) {
                                translate(size.width * 0.5f, size.height * 0.5f) {
                                    drawPath(roundedPolygonPath, color = Color(0xFFF15087))
                                }
                            }
                        }
                    }
                    .padding(0.dp)
                    .size(100.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                prevTime = System.currentTimeMillis()
                                viewModel.dragTimer.stopTimer()
                                viewModel.dragTimer.startTimer()
                                viewModel.animationTimer.stopTimer()

                            },
                            onDragEnd = {
                                println("tempo totale: $deltaSum")
                                dragTrajectory = _dragTrajectory.toList()
                                isFirstTime = false
                                println("traiettoria $dragTrajectory")
                                identifier++
                                //isShown = false
                                viewModel.dragTimer.pauseTimer()
                                viewModel.animationEmitter.emitTrajectory(dragTrajectory)
                                _dragTrajectory.clear()
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                            val currTime = System.currentTimeMillis()
                            val delta = (currTime - prevTime).toInt()
                            deltaSum += delta
                            if (isFirstTime) {
                                isFirstTime = false
                                val shapeStateHolder = StateHolder<Float, AnimationVector1D>(
                                    id = identifier,
                                    animationType = AnimationType.SHAPE,
                                    state = Start(
                                        visualDescriptor = MorphVisualDescriptor(
                                            currentValue = 0F,
                                            animationType = AnimationType.SHAPE,
                                            /*animationSpec = infiniteRepeatable(
                                                tween(2000, easing = LinearEasing),
                                                repeatMode = RepeatMode.Reverse,
                                            ),*/
                                            animationSpec = tween(
                                                durationMillis = 10000,
                                                easing = LinearEasing
                                            ),
                                            durationMillis = 10000,
                                            animatable = Animatable(
                                                initialValue = 0F,
                                                typeConverter = Float.VectorConverter
                                            ),
                                            isAnimated = true,
                                            shape1 = Shape.CustomPolygonalShape(
                                                roundedPolygon = shapeA,
                                                path = shapeA.toPath().asComposePath()
                                            ),
                                            shape2 = Shape.CustomPolygonalShape(
                                                roundedPolygon = shapeB,
                                                path = shapeB.toPath().asComposePath()
                                            ),
                                            targetValue = 1F
                                        ),
                                    )
                                )

                                val offsetStateHolder = StateHolder<Offset, AnimationVector2D>(
                                    id = identifier,
                                    state = Start(
                                        visualDescriptor = VisualDescriptor(
                                            currentValue = Offset(
                                                offsetX + (offsetX - prevOffset.x),
                                                offsetY + (offsetY - prevOffset.y)
                                            ),
                                            animationType = AnimationType.OFFSET,
                                            animationSpec = tween(
                                                durationMillis = delta,
                                                easing = LinearEasing
                                            ),
                                            animatable = Animatable(
                                                initialValue = Offset(
                                                    offsetX + (offsetX - prevOffset.x),
                                                    offsetY + (offsetY - prevOffset.y)
                                                ),
                                                typeConverter = Offset.VectorConverter
                                            ),
                                            isAnimated = true,
                                            durationMillis = delta
                                        )
                                    ),
                                    animationType = AnimationType.OFFSET,
                                    //wrappedStateHolders = listOf(shapeStateHolder)
                                )

                                _dragTrajectory.add(
                                    offsetStateHolder
                                )
                            }
                            else {
                                _dragTrajectory.add(
                                    StateHolder<Offset, AnimationVector>(
                                        id = identifier,
                                        state = State.Animated(
                                            animation = Animation(
                                                animationSpec = tween(
                                                    durationMillis = delta,
                                                    easing = LinearEasing
                                                ),
                                                targetValue = Offset(
                                                    offsetX + (offsetX - prevOffset.x),
                                                    offsetY + (offsetY - prevOffset.y)
                                                ),
                                                durationMillis = delta
                                            )
                                        ),
                                        animationType = AnimationType.OFFSET
                                    )
                                )
                            }
                            prevOffset = Offset(offsetX, offsetY)
                            prevTime = currTime
                        }
                    }
            ) {
                SideEffect { println("sono il box") }
            }
        }
    }
}

@Composable
fun Timer(timer: SampleViewModel.Timer, color: Color) {
    val timerValue by timer.timer.collectAsStateWithLifecycle()
    Text(
        text = timerValue.toInt().toString(),
        color = color,
        fontSize = 24.sp)
}

internal fun Float.toRadians() = this * PI.toFloat() / 180f

internal val PointZero = PointF(0f, 0f)

internal fun radialToCartesian(
    radius: Float,
    angleRadians: Float,
    center: PointF = PointZero
) = directionVectorPointF(angleRadians) * radius + center

internal fun directionVectorPointF(angleRadians: Float) =
    PointF(cos(angleRadians), sin(angleRadians))

