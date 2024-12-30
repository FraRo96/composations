package com.fraro.sample_app.ui.screens

import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.plus
import androidx.core.graphics.times
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.pill
import androidx.graphics.shapes.pillStar
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
import com.fraro.composable_realtime_animations.ui.screens.RealtimeBox
import com.fraro.sample_app.ui.viewmodels.SampleViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SampleScreen() {
    val context = LocalContext.current
    val localConfig = LocalConfiguration.current
    val density = LocalDensity.current
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

    val shapeA2 = remember {
        RoundedPolygon(
            8,
            rounding = CornerRounding(0.2f)
        )
    }
    val shapeB2 = remember {
        RoundedPolygon.pillStar(
            rounding = CornerRounding(0.2f)
        )
    }

    val shapeB3 = remember {
        RoundedPolygon.pill(
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

    val morph = remember {
        Morph(shapeA, shapeB)
    }

    val morph2 = remember {
        Morph(shapeB2, shapeA2)
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
    val animatedRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress"
    )

    val infiniteTransition2 = rememberInfiniteTransition("infinite outline movement 2")
    val animatedProgress2 = infiniteTransition2.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress2"
    )
    val animatedRotation2 = infiniteTransition2.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress2"
    )

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

    /*RealtimeBox(
        animationFlow = viewModel.animationEmitter.getTransformedFlow(),
        initialOffset = Offset(0f,0f),
        isStartedCallback = {
            viewModel.animationTimer.startTimer(); },
        isStoppedCallback = {
            viewModel.animationTimer.pauseTimer() }
    ) {

        Box(
            Modifier
                .clip(
                    CustomRotatingMorphShape(
                        morph,
                        animatedProgress.value,
                        animatedRotation.value
                    )
                )
                .background(Color.Red)
                .padding(0.dp)
                .size(100.dp)
        ) {
            SideEffect { println("Internal box") }
        }
    }*/
    Column(modifier = Modifier.fillMaxSize()) {

        /*RealtimeBox(
            animationFlow = viewModel.animationEmitter.getTransformedFlow(),
            initialOffset = Offset(0f,0f),
            isStartedCallback = {
                viewModel.animationTimer.startTimer(); },
            isStoppedCallback = {
                viewModel.animationTimer.pauseTimer() }
        ) {

            Box(
                Modifier
                    .clip(
                        CustomRotatingMorphShape(
                            morph,
                            animatedProgress.value,
                            animatedRotation.value
                        )
                    )
                    .background(Color.Red)
                    .padding(0.dp)
                    .size(100.dp)
            ) {
                SideEffect { println("Internal box") }
            }
        } */


        RealtimeBox(
            animationFlow = viewModel.animationEmitter.getTransformedFlow(),
            initialOffset = Offset(100f,300f),
            isStartedCallback = {
                viewModel.animationTimer.startTimer(); },
            isStoppedCallback = {
                viewModel.animationTimer.pauseTimer() }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 300.dp, height = 100.dp)
                    //.background(Color.Yellow.copy(0.3f))
                    .drawWithCache {
                        onDrawBehind {
                            val path = CustomRotatingMorphShape(
                                morph2,
                                animatedProgress2.value,
                                animatedRotation2.value
                            ).getPath()

                            val pathBounds = path.getBounds()
                            val pivotX = (pathBounds.topLeft.x + pathBounds.bottomRight.x) / 2F
                            val pivotY = (pathBounds.topLeft.y + pathBounds.bottomRight.y) / 2F
                            scale(
                                scale = size.height,
                                pivot = Offset(
                                    size.width / 2,
                                    size.height / 2
                                )
                            ) {
                                translate(
                                    left = size.width / 2,
                                    top = size.height / 2
                                ) {
                                    /*drawRect(
                                        size = size / 2f,
                                        topLeft = Offset(0f,0f),
                                        color = Color.Red
                                    )*/
                                    drawPath(
                                        path = path,
                                        color = Color.Yellow.copy(0.8f)
                                    )
                                }
                            }
                        }
                    }
            ) {
                SideEffect { println("Internal box") }
            }
        }
    }

    var isShown by remember { mutableStateOf(true) }

    var prevOffset by remember {
        mutableStateOf(Offset(0f,0f))
    }

    if (isShown) {
        val traj: MutableList<StateHolder<*, *>> = remember { mutableListOf() }
        val anim = remember { Animatable(
            initialValue = Offset(
                0f,
                0f
            ),
            typeConverter = Offset.VectorConverter) }

        val offsetStateHolder = remember {
            StateHolder<Offset, AnimationVector2D>(
                id = identifier,
                state = Start(
                    visualDescriptor = VisualDescriptor(
                        currentValue = Offset(
                            0f,
                            0f
                        ),
                        animationType = AnimationType.OFFSET,
                        animationSpec = tween(
                            durationMillis = 1000,
                            easing = LinearEasing
                        ),
                        animatable = anim,
                        isAnimated = true,
                        durationMillis = 1000
                    )
                ),
                animationType = AnimationType.OFFSET,
                //wrappedStateHolders = listOf(shapeStateHolder)
            )
        }
        val screenHeight = remember {
            with(density) { localConfig.screenHeightDp.dp.toPx() }
        }
        val screenWidth = remember {
            with(density) { localConfig.screenWidthDp.dp.toPx() }
        }

        traj.add(offsetStateHolder)

        calculateOffsets(
            maxScreenHeight = screenHeight,
            maxScreenWidth = screenWidth,
            numOffsets = 40
        ).forEach { currOffset ->
            traj.add(
                StateHolder<Offset, AnimationVector>(
                    id = identifier,
                    state = State.Animated(
                        animation = Animation(
                            animationSpec = tween(
                                durationMillis = 1000,
                                easing = LinearEasing
                            ),
                            targetValue = currOffset,
                            durationMillis = 1000
                        )
                    ),
                    animationType = AnimationType.OFFSET
                )
            )
        }
        traj.add(
            StateHolder(
                id = identifier,
                state = State.Stop,
                animationType = AnimationType.OFFSET,
                //wrappedStateHolders = TODO()
            )
        )

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                viewModel.animationEmitter.emitTrajectory(traj)
            } ) { Text("start") }
        }

        /*Box(modifier = Modifier.fillMaxSize()) {
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
                                    wrappedStateHolders = listOf(shapeStateHolder)
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
        } */
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

fun calculateOffsets(maxScreenWidth: Float, maxScreenHeight: Float, numOffsets: Int): List<Offset> {
    val offsets = mutableListOf<Offset>()
    for (i in 1..numOffsets) {
        val xi: Float = (maxScreenWidth / numOffsets) * i
        val yi: Float = (maxScreenHeight / numOffsets) * i
        offsets.add(Offset(xi, yi))
    }
    return offsets
}

fun calculateRandOffsets(maxScreenWidth: Float, maxScreenHeight: Float, numOffsets: Int): List<Offset> {
    val offsets = mutableListOf<Offset>()
    for (i in 1..numOffsets) {
        val xi = Random.nextInt(0, maxScreenWidth.toInt())
        val yi = Random.nextInt(0, maxScreenHeight.toInt())
        offsets.add(Offset(xi.toFloat(), yi.toFloat()))
    }
    return offsets
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
        //matrix.rotateZ(rotation)

        val path = morph.toPath(progress = percentage).asComposePath()
        path.transform(matrix)

        return Outline.Generic(path)
    }

    fun getPath() = morph.toPath(progress = percentage).asComposePath()
}



