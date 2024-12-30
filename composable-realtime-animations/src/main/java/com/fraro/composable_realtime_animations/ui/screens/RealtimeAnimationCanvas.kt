package com.fraro.composable_realtime_animations.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fraro.composable_realtime_animations.data.models.AnimationType
import com.fraro.composable_realtime_animations.data.models.MorphVisualDescriptor
import com.fraro.composable_realtime_animations.data.models.State
import com.fraro.composable_realtime_animations.data.models.State.*
import com.fraro.composable_realtime_animations.data.models.StateHolder
import com.fraro.composable_realtime_animations.data.models.VisualDescriptor
import com.fraro.composable_realtime_animations.data.models.offsetDefault
import com.fraro.composable_realtime_animations.data.models.shapeDefault
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
import kotlin.math.roundToInt
import kotlin.random.Random

val randomColor
    get() = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RealtimeAnimationCanvas(
    animationFlow: StateFlow<Map<Long, StateHolder<*,*>>>,
    samplingInterval: Int = 100,
    isStartedCallback: (() -> Unit)? = null,
    isStoppedCallback: (() -> Unit)? = null
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

        val elements = remember {
            mutableMapOf<Long, MutableMap<AnimationType, VisualDescriptor<*,*>>>()
        }

        val collectedFlow by animationFlow.collectAsStateWithLifecycle(
            initialValue = mapOf(),
            minActiveState = Lifecycle.State.RESUMED
        )

        LaunchedEffect(key1 = collectedFlow) {

            animateCanvas(
                collectedFlow,
                elements,
                coroutineScope,
                isStartedCallback,
                isStoppedCallback
            )
        }

        val color = remember { Color(0xFFF15087) }

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
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current

        val screenHeight = remember {
            with (density) {
                configuration.screenHeightDp.dp.toPx()
            }
        }
        val screenWidth = remember {
            with (density) {
                configuration.screenWidthDp.dp.toPx()
            }
        }
        val infiniteTransition = rememberInfiniteTransition("infinite outline movement")
        val infiniteTransition2 = rememberInfiniteTransition("infinite offset movement")
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

        /*val animatedOffsetX = infiniteTransition2.animateFloat(
            initialValue = 0f,
            targetValue = screenWidth,
            animationSpec = infiniteRepeatable(
                tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "animatedOffsetXProgress"
        )

        val animatedOffsetY = infiniteTransition2.animateFloat(
            initialValue = 0f,
            targetValue = screenHeight,
            animationSpec = infiniteRepeatable(
                tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "animatedOffsetYProgress"
        )*/

        /*var offsetY by remember { mutableStateOf(Animatable(0f)) }

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                offsetY.animateTo(
                    targetValue = screenHeight, // target position
                    animationSpec = tween(durationMillis = 50000)
                )
            }
        }*/

        var offset by remember { mutableStateOf(Offset(0f,0f)) }

        elements.values.forEach { element ->
            offset = (element[AnimationType.OFFSET]
                ?.getStaticOrAnimatedValue() ?: offsetDefault) as Offset
        }

        MovingBox(offset) {
            Box(
                Modifier
                    .clip(
                        CustomRotatingMorphShape(
                            morph,
                            animatedProgress.value,
                            animatedRotation.value
                        )
                    )
                    .padding(0.dp)
                    .background(color)
                    .size(100.dp)
            ) {
                SideEffect { println("Internal box") }
            }
        }
    }
}

@Composable
fun MovingBox(
    offset: Offset,
    function:  @Composable () -> Unit
) {
    SideEffect { println("Moving box") }
    Box(modifier = Modifier.padding(16.dp)
        .offset {
            IntOffset(
                offset.x.roundToInt(),
                offset.y.roundToInt()
            )
        }
    ) {
        function()
    }
}

suspend fun <T,V> animateDescriptor(
    descriptors: MutableMap<AnimationType, VisualDescriptor<*,*>>,
    descriptor: VisualDescriptor<T,*>?,
    visualUpdate: State<out V,*>,
    isStartedCallback: (() -> Unit)?,
    isStoppedCallback: (() -> Unit)?,
) {

    /* same map index, same type */
    val visualUpdateCast = visualUpdate as State<out T,*>

    when (visualUpdateCast) {
        is Pause -> {
            descriptor?.stopAnimation()
        }

        is Fixed -> {
            descriptor?.animateTo(
                targetValue = visualUpdateCast.targetValue
            )
        }

        is Animated -> {
            descriptor?.animateTo(
                durationMillis = visualUpdateCast.animation.durationMillis,
                targetValue = visualUpdateCast.animation.targetValue,
                animationSpec = visualUpdateCast.animation.animationSpec as AnimationSpec<T>
            )
        }

        is Stop -> {
            descriptor?.let {
                descriptors.remove(it.animationType)
            }
            isStoppedCallback?.invoke()
        }

        is Start -> {
            descriptors[visualUpdateCast.visualDescriptor.animationType] = visualUpdateCast.visualDescriptor
            isStartedCallback?.invoke()
        }
    }
}

fun animateCanvas(
    flow: Map<Long, StateHolder<*,*>>,
    elements: MutableMap<Long, MutableMap<AnimationType, VisualDescriptor<*,*>>>,
    coroutineScope: CoroutineScope,
    isStartedCallback: (() -> Unit)?,
    isStoppedCallback: (() -> Unit)?
) {
    flow.forEach { flowStateHolder ->
        coroutineScope.launch {
            elements[flowStateHolder.key]?.let { existentDescriptors ->
                flowStateHolder.value.getState().forEach {
                                                    animType, visualUpdate ->
                    coroutineScope.launch {
                        animateDescriptor(
                            existentDescriptors,
                            existentDescriptors[animType],
                            visualUpdate,
                            isStartedCallback,
                            isStoppedCallback
                        )
                    }
                }
            }
            val state = flowStateHolder.value.getPartialState()
            if (state is Start<*,*>) {
                isStartedCallback?.invoke()
                val map = mutableMapOf<AnimationType, VisualDescriptor<*, *>>()

                val states = flowStateHolder.value.getState()
                states.forEach { st ->
                    val otherState = st.value
                    if (otherState is Start<*,*>) {
                        map[otherState.visualDescriptor.animationType] =
                            otherState.visualDescriptor
                        coroutineScope.launch {
                            otherState.visualDescriptor.animateTo()
                        }
                    }
                }
                elements[flowStateHolder.key] = map
            }
        }
    }
}

@OptIn(FlowPreview::class)
fun Flow<StateHolder<*,*>>.toBatchedStateFlow(
    samplingInterval: Long = 50L
): StateFlow<Map<Long, StateHolder<*,*>>> {
    val sharedMap = mutableMapOf<Long, StateHolder<*,*>>() // Shared map for the latest particles

    return this
        .onEach { stateHolder ->
            // Add the particle to the shared map
            val state = sharedMap[stateHolder.id]?.getPartialState()
            if (state !is Start) {
                sharedMap[stateHolder.id] = stateHolder
            }
        }
        .sample(samplingInterval) // Emit updates at the specified interval
        .map {
            // Create a snapshot of the map to emit
            val snapshot = sharedMap.toMap()
            sharedMap.clear()
            println("emissione")
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
) : Shape {

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
}