package com.fraro.composable_realtime_animations.ui.screens

import android.annotation.SuppressLint
import com.fraro.composable_realtime_animations.data.models.Size.DoubleAxisMeasure
import com.fraro.composable_realtime_animations.data.models.Size.SingleAxisMeasure
import com.fraro.composable_realtime_animations.data.models.Size.RescaleFactor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan2
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RealtimeAnimationCanvas(
    animationFlow: StateFlow<Map<Long, ParticleVisualizationModel>?>,
    samplingInterval: Int,
    isForward: Boolean,
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
            mutableStateMapOf<Long, ParticleAnimationModel>()
        }

        val collectedFlow by animationFlow.collectAsStateWithLifecycle(
            initialValue = null,
            minActiveState = Lifecycle.State.RESUMED
        )

        LaunchedEffect(key1 = collectedFlow) {
            collectedFlow?.let { map ->
                if (isForward) {
                    //createAnimationIntoFuture(map, particlesAnimMap, coroutineScope)
                }
                else {
                    createAnimationFromPast(map, particlesAnimMap, coroutineScope)
                }
            }
        }
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
                    centerX = radius / 2 + offset.x,
                    centerY = radius / 2 + offset.y
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
                val scale = size?.scale ?: 100F
                val pathBounds = path.getBounds()
                val pivotX = (pathBounds.topLeft.x + pathBounds.bottomRight.x) * scale / 2F
                val pivotY = (pathBounds.topLeft.y + pathBounds.bottomRight.y) * scale / 2F

                rotate(
                    degrees = heading ?: 0F,
                    pivot = Offset(
                        x = offset.x + pivotX,
                        y = offset.y + pivotY
                    )
                ) {
                    scale(
                        scale = scale,
                        pivot = Offset(pivotX, pivotY)
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

            particlesAnimMap.forEach { entry ->

                val isInScreenRange = entry.value.animatedOffset?.value?.let {
                    it.x >= 0 && it.x <= configuration.screenWidthDp.dp.toPx() &&
                    it.y >= 0 && it.y <= configuration.screenHeightDp.dp.toPx()
                } ?: true // false if you want screen constraints

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

fun saludaAndonio() {
    println("saluda Andonio")
}

fun createAnimationFromPast(
    map: Map<Long, ParticleVisualizationModel>,
    particlesAnimMap: SnapshotStateMap<Long, ParticleAnimationModel>,
    coroutineScope: CoroutineScope
) {
    saludaAndonio()
    map.forEach { particle ->
        println("posizione# ${particle.value.screenPosition}")
        val foundAnimation = particlesAnimMap[particle.key]
        foundAnimation?.let { found ->
            val currentScreenPosition = findStaticOrAnimatedCurrentScreenPosition(found)
            val nextScreenPosition = particle.value.screenPosition
            println("pos corrente ${currentScreenPosition.offset}")
            println("pos prossima ${nextScreenPosition.offset}")
            /*val nextAAScreenPosition = ScreenPosition(
                offset = nextUnscaledScreenPosition.offset
                        + (nextUnscaledScreenPosition.offset
                        - currentScreenPosition.offset) * particle.value.maximumDelayFraction,
                heading = nextUnscaledScreenPosition.heading
                        + nextUnscaledScreenPosition.heading * particle.value.maximumDelayFraction
            )*/
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
                duration = particle.value.duration + (particle.value.duration * particle.value.maximumDelayFraction).roundToInt()
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

fun createAnimationIntoFuture(
    map: ConcurrentHashMap<Long, ParticleVisualizationModel>,
    particlesAnimMap: SnapshotStateMap<Long, ParticleAnimationModel>,
    coroutineScope: CoroutineScope
) {
    map.forEach { particle ->
        particle.value.directionUnitVector?.let { direction ->
            val foundAnimation = particlesAnimMap[particle.key]
            val currentScreenPosition = findScreenPosition(foundAnimation)
                ?: particle.value.screenPosition

            val nextScreenPosition = ScreenPosition(
                offset = currentScreenPosition.offset +
                        Offset (
                            particle.value.duration * direction.x,
                            particle.value.duration * direction.y
                        ),
                heading = angleFromUnitVector(direction.x, direction.y)
            )
            CoroutineScope(Dispatchers.Main).launch {
                foundAnimation?.animatedOffset?.stop()
                foundAnimation?.animatedHeading?.stop()
            }
            val animatedOffset = Animatable(currentScreenPosition.offset, Offset.VectorConverter)
            val animatedHeading = Animatable(currentScreenPosition.heading, Float.VectorConverter)

            particlesAnimMap[particle.key] = ParticleAnimationModel(
                prev = currentScreenPosition,
                next = nextScreenPosition,
                animatedHeading = animatedHeading,
                animatedOffset = animatedOffset,
                particleVisualizationModel = particle.value,
                duration = particle.value.duration + (particle.value.duration * particle.value.maximumDelayFraction).roundToInt()
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
    }
}

fun findStaticOrAnimatedCurrentScreenPosition(found: ParticleAnimationModel): ScreenPosition {
    val currOffset = found.animatedOffset?.value ?: found.next.offset
    val currHeading = found.animatedHeading?.value ?: found.next.heading
    println("offset: $currOffset")
    return ScreenPosition(currOffset, currHeading)
}

fun findScreenPosition(found: ParticleAnimationModel?): ScreenPosition? {
    found?.let {
        var currOffset = it.next.offset
        var currHeading = it.next.heading
        it.animatedOffset?.value?.let { currentAnimatedOffset ->
            currOffset = currentAnimatedOffset
        }
        it.animatedHeading?.value?.let { currentAnimatedHeading ->
            currHeading = currentAnimatedHeading
        }
        return ScreenPosition(currOffset, currHeading)
    }
    return null
}

fun angleFromUnitVector(x: Float, y: Float): Float {
    val angleRadians = atan2(y, x)
    val angleDegrees = Math.toDegrees(angleRadians.toDouble()).toFloat()
    return if (angleDegrees < 0) angleDegrees + 360 else angleDegrees
}

@OptIn(FlowPreview::class)
fun Flow<ParticleVisualizationModel>.toStateFlowWithLatestValues(
    samplingInterval: Long = 100L
): StateFlow<Map<Long, ParticleVisualizationModel>?> {
    return this
        .map { particle ->
            val snapshotMap =
                mutableMapOf<Long, ParticleVisualizationModel>() // Create a new map
            snapshotMap[particle.id] = particle // Add the particle to the new map
            println("mappa $snapshotMap")
            snapshotMap // Return the new map
        }
        .sample(samplingInterval)
        //.buffer()
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
