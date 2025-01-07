package com.fraro.sample_app.ui.screens

import com.fraro.sample_app.R
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.pillStar
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.Animation
import com.fraro.composable_realtime_animations.data.models.AnimationType
import com.fraro.composable_realtime_animations.data.models.State
import com.fraro.composable_realtime_animations.data.models.State.Start
import com.fraro.composable_realtime_animations.data.models.StateHolder
import com.fraro.composable_realtime_animations.data.models.VisualDescriptor
import com.fraro.composable_realtime_animations.ui.screens.RealtimeBox
import com.fraro.sample_app.ui.viewmodels.SampleViewModel
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SampleScreen() {
    val context = LocalContext.current
    val localConfig = LocalConfiguration.current
    val density = LocalDensity.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: SampleViewModel = ViewModelProvider(lifecycleOwner)[SampleViewModel::class.java]

    val screenHeight = with (density) { localConfig.screenHeightDp.dp.toPx() }
    val screenHeightDp = localConfig.screenHeightDp.dp
    val screenWidth = with (density) { localConfig.screenWidthDp.dp.toPx() }
    val screenWidthDp = localConfig.screenWidthDp.dp

    val dodecagonPoly = remember {
        RoundedPolygon(
            12,
            rounding = CornerRounding(0.2f)
        )
    }

    val trianglePoly = remember {
        RoundedPolygon.star(
            3,
            innerRadius = 0.4f,
            rounding = CornerRounding(0.2f)
        )
    }

    val hexagonStarPoly = remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 7,
            innerRadius = 0.4f,
            //rounding = CornerRounding(0.5f),
            innerRounding = CornerRounding(0.5f)
        )
    }

    val dodecagonStarPoly = remember {
        RoundedPolygon.star(
            numVerticesPerRadius = 12,
            rounding = CornerRounding(0.2f)
        )
    }

    val octagonPoly = remember {
        RoundedPolygon(
            8,
            rounding = CornerRounding(0.2f)
        )
    }

    val pillStarMultipleVertsPoly = remember {
        RoundedPolygon.pillStar(
            rounding = CornerRounding(0f),
            numVerticesPerRadius = 200
        )
    }

    val circularMorph = remember {
        Morph(dodecagonPoly, dodecagonStarPoly)
    }

    val cloudMorph = remember {
        Morph(octagonPoly, pillStarMultipleVertsPoly)
    }

    val engineMorph = remember {
        Morph(hexagonStarPoly, trianglePoly)
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

    val animatedProgress2 = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress2"
    )
    val animatedRotation2 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress2"
    )

    val animatedProgress3 = infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress2"
    )

    val animatedProgress4 = infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedMorphProgress3"
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

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            Modifier
                .clip(
                    CustomRotatingMorphShape(
                        circularMorph,
                        animatedProgress.value,
                        animatedRotation.value
                    )
                )
                .offset()
                .background(Color.Yellow)
                .padding(0.dp)
                .size(100.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(0.dp, 60.dp)
                .size(width = 300.dp, height = 100.dp)
                //.background(Color.Yellow.copy(0.3f))
                .drawWithCache {
                    onDrawBehind {
                        val path = CustomRotatingMorphShape(
                            cloudMorph,
                            animatedProgress2.value,
                            animatedRotation2.value
                        ).getPath()

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
                                drawPath(
                                    path = path,
                                    color = Color.Cyan.copy(0.3f)
                                )
                            }
                        }
                    }
                }
        ) {}

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(150.dp, 50.dp)
                .size(width = 340.dp, height = 120.dp)
                //.background(Color.Yellow.copy(0.3f))
                .drawWithCache {
                    onDrawBehind {
                        val path = CustomRotatingMorphShape(
                            cloudMorph,
                            animatedProgress2.value,
                            animatedRotation2.value
                        ).getPath()

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
                                drawPath(
                                    path = path,
                                    color = Color.Cyan.copy(0.3f)
                                )
                            }
                        }
                    }
                }
        ) {}

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(50.dp, 150.dp)
                .size(width = 100.dp, height = 80.dp)
                //.background(Color.Yellow.copy(0.3f))
                .drawWithCache {
                    onDrawBehind {
                        val path = CustomRotatingMorphShape(
                            cloudMorph,
                            animatedProgress2.value,
                            animatedRotation2.value
                        ).getPath()

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
                                    color = Color.Cyan.copy(0.1f)
                                )
                            }
                        }
                    }
                }
        ) {}
        RealtimeBox(
            animationFlow = viewModel.animationEmitter.getTransformedFlow(),
            initialOffset = Offset(
                x = (screenWidth / 1.7).toFloat(),
                y = (screenHeight / 3).toFloat()),
            initialRotation = Random.nextInt(-45, 45).toFloat(),
            isStartedCallback = {
                viewModel.animationTimer.startTimer(); },
            isStoppedCallback = {
                viewModel.animationTimer.pauseTimer() }
        ) {
            /*Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    //.size(width = (Random.nextInt(20, 70)).dp, height = (Random.nextInt(10, 40)).dp)
                    //.background(Color.Black.copy(0.3f))
            ) {*/
                Box(
                    Modifier
                        .drawWithCache {
                            onDrawBehind {
                                val path = CustomRotatingMorphShape(
                                    engineMorph,
                                    animatedProgress4.value,
                                    animatedRotation2.value
                                ).getPath()

                                scale(
                                    scale = size.height,
                                    pivot = Offset(
                                        size.width / 2,
                                        size.height / 2
                                    )
                                ) {
                                    translate(
                                        left = size.width / 1.79f,
                                        top = size.height / 1.80f
                                    ) {
                                        drawPath(
                                            path = path,
                                            color = Color.Red.copy(0.8f)
                                        )
                                    }
                                }
                            }
                        }
                        //.offset(1500.dp, 70.dp)
                        //.background(Color.Red)
                        .padding(0.dp)
                        .size(20.dp)
                )
                Image(
                   painter = painterResource(R.drawable.car),
                   contentDescription = "bird",
                   modifier = Modifier.size(100.dp)
                )
            //}
        }

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Box(
                Modifier
                    .size(width = screenWidthDp, height = 100.dp)
                    .offset(0.dp, min(screenHeightDp, screenWidthDp) / 4)
                    .drawWithCache {
                        onDrawBehind {
                            val path = CustomRotatingMorphShape(
                                cloudMorph,
                                animatedProgress3.value,
                                animatedRotation2.value
                            ).getPath()

                            scale(
                                scale = size.width,
                                pivot = Offset(
                                    size.width / 2,
                                    size.height / 2
                                )
                            ) {
                                translate(
                                    left = size.width / 1.9995f,
                                    top = size.height / 1.993f
                                ) {
                                    /*drawRect(
                                        size = size / 2f,
                                        topLeft = Offset(0f,0f),
                                        color = Color.Red
                                    )*/
                                    drawPath(
                                        path = path,
                                        color = Color.Green.copy(0.3f)
                                    )
                                }
                            }
                        }
                    }
                    //.background(Color.Green)
                    .padding(0.dp)
            ) {
                SideEffect { println("Internal box") }
            }
        }
    }

    var isShown by remember { mutableStateOf(true) }

    var identifier by remember { mutableStateOf(0L) }

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

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                traj.add(offsetStateHolder)

                calculateRandOffsets(
                    maxScreenHeight = screenHeight,
                    maxScreenWidth = screenWidth,
                    numOffsets = 10
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
                viewModel.animationEmitter.emitTrajectory(traj.toList())
            } ) { Text("start") }
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




