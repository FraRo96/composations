package com.fraro.sample_app.ui.screens

import androidx.compose.animation.core.LinearEasing
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
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.ui.screens.RealtimeAnimationCanvas
import com.fraro.sample_app.data.TrajectoryPoint
import com.fraro.sample_app.ui.viewmodels.SampleViewModel
import com.fraro.sample_app.ui.viewmodels.SimulationViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SampleScreen() {
    val context = LocalContext.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: SampleViewModel = ViewModelProvider(lifecycleOwner)[SampleViewModel::class.java]

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
        samplingInterval = 50,
        isForward = false,
        easing = LinearEasing,
        isStartedCallback = {
            viewModel.animationTimer.startTimer(); }
    )

    var isShown by remember { mutableStateOf(true) }

    if (isShown) {
        Box(modifier = Modifier.fillMaxSize()) {
            var offsetX by remember { mutableStateOf(0f) }
            var offsetY by remember { mutableStateOf(0f) }
            var prevTime = 0L
            var deltaSum = 0

            val _dragTrajectory: MutableList<TrajectoryPoint> = remember { mutableListOf() }
            var dragTrajectory: List<TrajectoryPoint>
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
                            _dragTrajectory.add(
                                TrajectoryPoint(
                                    identifier,
                                    Offset(offsetX, offsetY),
                                    delta
                                )
                            )
                            prevTime = currTime
                        }
                    }
            )
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

