package com.fraro.sample_app.ui.screens

import android.graphics.PointF
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.plus
import androidx.core.graphics.times
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.composable_realtime_animations.data.models.Size.SingleAxisMeasure
import com.fraro.sample_app.data.SimulationActor
import com.fraro.sample_app.data.Trace
import com.fraro.sample_app.ui.theme.Indigo
import com.fraro.sample_app.ui.viewmodels.SampleViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreparationScreen(
    onSubmitClick: () -> Unit
) {

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: SampleViewModel = ViewModelProvider(lifecycleOwner)[SampleViewModel::class.java]

    var isTraceDropDownExpanded by remember { mutableStateOf(false) }
    var isShapeDropDownExpanded by remember { mutableStateOf(false) }
    var trace by remember { mutableStateOf(Trace.DIAGONAL) }
    var shape: Shape by remember { mutableStateOf(Shape.Ellipse()) }
    var speedSliderRange by remember { mutableStateOf(1f..1000f) }
    var rotationSliderRange by remember { mutableStateOf(0f..360f) }
    var isClockwise by remember { mutableStateOf(true) }
    var currentParticle by remember { mutableStateOf(0L) }
    var shapeColor by remember { mutableStateOf(Indigo) }

    viewModel.screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    viewModel.screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

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

    val polygon = remember(vertices, rounding) {
        RoundedPolygon(
            vertices = vertices,
            perVertexRounding = rounding
        )
    }

    val roundedPolygonPath = remember {
        polygon
            .toPath()
            .asComposePath()
    }

    Column(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PreparationScreenTitle()
        Row(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Shape: ",
                fontWeight = FontWeight.Bold
            )
            Box(
                //modifier = Modifier.padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.clickable {
                        isShapeDropDownExpanded = true
                    }
                ) {
                    Text(text = shape!!.description)
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown of shapes",
                        //modifier = Modifier.background(Color.White, RoundedCornerShape(70))
                    )
                }
                DropdownMenu(
                    expanded = isShapeDropDownExpanded!!,
                    onDismissRequest = {
                        isShapeDropDownExpanded = false
                    }
                ) {
                    Shape.listShapes(200F, roundedPolygonPath).forEachIndexed { _, shapeValue ->
                        if (!(shapeValue is Shape.Unspecified)) {

                            DropdownMenuItem(
                                text = {
                                    Text(text = shapeValue.description)
                                },
                                onClick = {
                                    isShapeDropDownExpanded = false
                                    shape = shapeValue
                                }
                            )
                        }
                    }
                }
            }
        }
        OutlinedButton(
            modifier = Modifier.padding(bottom = 10.dp),
            border = BorderStroke(1.dp, shapeColor),
            onClick = { shapeColor = generateRandomColor() }) {

                Text(text ="Change color")
        }
        GeometricThumbnail(
            shape = shape,
            shapeColor = shapeColor,
            customPath = roundedPolygonPath,
            changeNVerticesCallback = { delta ->
                val polygon = shape as Shape.RegularPolygon
                if (delta == 1 && polygon.nVertices < 20) {

                    shape = Shape.RegularPolygon(
                        nVertices = (shape!! as Shape.RegularPolygon).nVertices + 1,
                        size = polygon.size
                    )
                } else if (delta == -1 && polygon.nVertices > 3) {

                    shape = Shape.RegularPolygon(
                        nVertices = polygon.nVertices - 1,
                        size = polygon.size
                    )
                }
            }
        )

        Row(
        ) {
            OutlinedButton(
                onClick = {
                    currentParticle--
                    isTraceDropDownExpanded = false
                    isShapeDropDownExpanded = false
                    trace = viewModel.simulationModel[currentParticle]!!.trace
                    shape = viewModel.simulationModel[currentParticle]!!.shape
                    speedSliderRange = viewModel.simulationModel[currentParticle]!!.speed.first.toFloat()..viewModel.simulationModel[currentParticle]!!.speed.second.toFloat()
                    rotationSliderRange = viewModel.simulationModel[currentParticle]!!.rotation.first.toFloat()..viewModel.simulationModel[currentParticle]!!.rotation.second.toFloat()
                    isClockwise = viewModel.simulationModel[currentParticle]!!.isRotationClockwise
                    shapeColor = viewModel.simulationModel[currentParticle]!!.color
                },
                enabled = (currentParticle > 0)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    contentDescription = "Go to previous particle"
                )
            }
            Spacer(modifier = Modifier.weight(0.1f))
            Text(text = "#${currentParticle + 1}", style = MaterialTheme.typography.titleLarge, color = shapeColor)
            Spacer(modifier = Modifier.weight(0.1f))
            OutlinedButton(onClick = {
                viewModel.simulationModel[currentParticle] = SimulationActor(
                    shape = shape,
                    trace = trace,
                    rotation = Pair(rotationSliderRange.start.roundToInt(), rotationSliderRange.endInclusive.roundToInt()),
                    speed = Pair(speedSliderRange.start.roundToInt(), speedSliderRange.endInclusive.roundToInt()),
                    isRotationClockwise = isClockwise,
                    color = shapeColor
                )
                currentParticle++
                if (currentParticle < viewModel.simulationModel.size) {
                    isTraceDropDownExpanded = false
                    isShapeDropDownExpanded = false
                    trace = viewModel.simulationModel[currentParticle]!!.trace
                    shape = viewModel.simulationModel[currentParticle]!!.shape
                    speedSliderRange = viewModel.simulationModel[currentParticle]!!.speed.first.toFloat()..viewModel.simulationModel[currentParticle]!!.speed.second.toFloat()
                    rotationSliderRange = viewModel.simulationModel[currentParticle]!!.rotation.first.toFloat()..viewModel.simulationModel[currentParticle]!!.rotation.second.toFloat()
                    isClockwise = viewModel.simulationModel[currentParticle]!!.isRotationClockwise
                    shapeColor = viewModel.simulationModel[currentParticle]!!.color
                }
                else {
                    isTraceDropDownExpanded = false
                    isShapeDropDownExpanded = false
                    trace = Trace.DIAGONAL
                    shape = Shape.RegularPolygon(6, SingleAxisMeasure(200F))
                    speedSliderRange = 0f..100f
                    rotationSliderRange = 0f..360f
                    isClockwise = true
                    shapeColor = Indigo
                }
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    contentDescription = "Go to next particle"
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))

        SpeedSlider(
            sliderPosition = speedSliderRange,
            color = shapeColor,
            callback = { range ->
                speedSliderRange = range
            }
        )
        Spacer(modifier = Modifier.height(25.dp))
        RotationSlider(
            sliderPosition = rotationSliderRange,
            color = shapeColor,
            isClockwise = isClockwise,
            rangeCallback = { range ->
                rotationSliderRange = range
            },
            directionCallback = { dir ->
                isClockwise = dir
            }
        )
        Spacer(modifier = Modifier.height(30.dp))

        Column(
        ) {
            Row {
                Text(
                    text = "Trace: ",
                    fontWeight = FontWeight.Bold
                )
                Box(
                    //modifier = Modifier.padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.clickable {
                            isTraceDropDownExpanded = true
                        }
                    ) {
                        Text(text = trace!!.description)
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown of traces",
                            //modifier = Modifier.background(Color.White, RoundedCornerShape(70))
                        )
                    }
                    DropdownMenu(
                        expanded = isTraceDropDownExpanded,
                        onDismissRequest = {
                            isTraceDropDownExpanded = false
                        }
                    ) {
                        Trace.values().forEachIndexed { index, traceValue ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = traceValue.description)
                                },
                                onClick = {
                                    isTraceDropDownExpanded = false
                                    trace = traceValue
                                }
                            )
                        }
                    }
                }
            }
            }
        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = { viewModel.startFlow(); onSubmitClick() },
            modifier = Modifier.padding(top = 25.dp)
        ) {
            Text(text = "Start animation!")
        }
    }
}

fun generateRandomColor(): Color {
    val base = Random.nextFloat() * 0.5f + 0.25f // Generates a base around mid-range for low saturation
    val variation = Random.nextFloat() * 0.2f    // Slightly larger variation for more color diversity

    // Randomly adjust each component by a small variation to cover more color shades
    val red = (base + if (Random.nextBoolean()) variation else -variation).coerceIn(0f, 1f)
    val green = (base + if (Random.nextBoolean()) variation else -variation).coerceIn(0f, 1f)
    val blue = (base + if (Random.nextBoolean()) variation else -variation).coerceIn(0f, 1f)

    return Color(
        red = red,
        green = green,
        blue = blue,
        alpha = 1f
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedSlider(
    sliderPosition: ClosedFloatingPointRange<Float>,
    color: Color,
    callback: (ClosedFloatingPointRange<Float>) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SliderTitle(text = "Speed (dp/s)")
        RangeSlider(
            value = sliderPosition,
            colors = SliderDefaults.colors(
                activeTrackColor = color,
                thumbColor = color.darker(),
            ),
            //steps = 1000,
            onValueChange = { range -> callback(range) },
            valueRange = 1f..1000f,
            onValueChangeFinished = {
                // launch some business logic update with the state you hold
                // viewModel.updateSelectedSliderValue(sliderPosition)
            },
        )
        SliderSubText(
            text = "Min: ${sliderPosition.start.roundToInt()}, Max: ${sliderPosition.endInclusive.roundToInt()}",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationSlider(
    sliderPosition: ClosedFloatingPointRange<Float>,
    color: Color,
    isClockwise: Boolean,
    rangeCallback: (ClosedFloatingPointRange<Float>) -> Unit,
    directionCallback: (Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val directionStr = if (isClockwise) "(clockwise)" else "(counter-clockwise)"

        SliderTitle(text = "Angle of rotation")
        //SliderSubTitle(text = directionStr)
        if (isClockwise) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "clockwise")
        } else {
            Icon(
                modifier = Modifier.graphicsLayer { rotationY = 180f },
                imageVector = Icons.Default.Refresh,
                contentDescription = "counter-clockwise"
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row() {
                RangeSlider(
                    value = sliderPosition,
                    modifier = Modifier.padding(end = 20.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = color,
                        thumbColor = color.darker()
                    ),
                    steps = 360,
                    onValueChange = { range -> rangeCallback(range) },
                    valueRange = 0f..360f,
                    onValueChangeFinished = {
                        // launch some business logic update with the state you hold
                        // viewModel.updateSelectedSliderValue(sliderPosition)
                    },
                )
                Checkbox(
                    checked = isClockwise,
                    onCheckedChange = { dir -> directionCallback(dir) },
                    colors = CheckboxDefaults.colors(checkedColor = color.darker())
                )
            }
            SliderSubText(
                text = "Min: ${sliderPosition.start.roundToInt()}, Max: ${sliderPosition.endInclusive.roundToInt()}"
            )
        }


    }
}

@Composable
fun SliderSubText(text: String) {
    Text(text = text, fontSize = 12.sp)
}

@Composable
fun PreparationScreenTitle() {
    Text(
        text = "Setup",
        style = MaterialTheme.typography.titleLarge,
        fontSize = 40.sp,
        modifier = Modifier
            .padding(20.dp)
            //.padding(bottom = 10.dp)
    )
}

@Composable
fun SliderTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        //fontSize = 20.sp,
        modifier = Modifier
            .padding(top = 10.dp)
    )
}

@Composable
fun SliderSubTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        //fontSize = 20.sp,
        modifier = Modifier
    )
}

@Composable
fun GeometricThumbnail(shape: Shape, customPath: Path, shapeColor: Color, changeNVerticesCallback: (Int) -> Unit) {

    when (shape) {
        is Shape.RegularPolygon -> {
            val halfSizeDp = (shape.size.size / 2F).dp
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                IconButton(
                    onClick = { changeNVerticesCallback(-1) },
                    modifier = Modifier.padding(
                        top = halfSizeDp / 2F,
                        //start = halfSizeDp
                    )
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "remove vertex")
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    //horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        //.fillMaxWidth()
                        //.fillMaxHeight()
                        //.background(Color.Red)
                        .padding(
                            top = (halfSizeDp / 2) + 24.dp,
                            bottom = halfSizeDp,
                            start = halfSizeDp,
                            end = halfSizeDp
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .drawWithCache {
                                val roundedPolygon = RoundedPolygon(
                                    numVertices = shape.nVertices,
                                    radius = shape.size.size,
                                    centerX = 0F,
                                    centerY = 0F, //(shape.size!!.size / 2F) + 90F
                                )
                                val polygonPath = roundedPolygon
                                    .toPath()
                                    .asComposePath()
                                onDrawBehind {
                                    drawPath(polygonPath, color = shapeColor)
                                }
                            }
                        //.fillMaxHeight(0.5f)
                    )
                }
                IconButton(
                    onClick = { changeNVerticesCallback(1) },
                    modifier = Modifier.padding(
                        top = halfSizeDp / 2,
                        //start = halfSizeDp
                    )
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "add vertex")
                }
            }
        }
        is Shape.Ellipse -> {
            Column(modifier = Modifier.padding(bottom = 100.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    val radiusX = 200F
                    val radiusY = 100F

                    drawOval(
                        color = shapeColor,
                        topLeft = Offset(size.width / 2 - radiusX, 0F),
                        size = Size(width = radiusX * 2, height = radiusY * 2),
                        style = Fill
                    )
                }
            }
        }
        is Shape.Rectangle -> {
            Column(modifier = Modifier.height(100.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    val radiusX = 200F
                    val radiusY = 100F

                    drawRect(
                        color = shapeColor,
                        topLeft = Offset(size.width / 2 - radiusX, 0F),
                        size = Size(width = radiusX * 2, height = radiusY * 2),
                        style = Fill
                    )
                }
            }
        }
        is Shape.Segment -> {
            Column(modifier = Modifier.height(100.dp), verticalArrangement = Arrangement.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    val length = 200F

                    drawLine(
                        color = shapeColor,
                        start = Offset(size.width / 2 - length, 100f),
                        end = Offset(size.width / 2 + length, 25f),
                        strokeWidth = 10F
                    )
                }
            }
        }
        is Shape.CustomPolygonalShape -> {

            Box(
                modifier = Modifier
                    .padding(bottom = 30.dp)
                    .drawWithCache {
                        onDrawBehind {
                            scale(100f, 100f) {
                                translate(size.width * 0.5f, size.height * 0.5f) {
                                    drawPath(customPath, color = Color(0xFFF15087))
                                }
                            }
                        }
                    }
                    .size(70.dp)
            )
        }
        else -> {

        }
    }
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


fun Color.darker(): Color {
    // Convert RGB to HSL
    val r = red
    val g = green
    val b = blue

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min

    val l = (max + min) / 2
    var s = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2 * l - 1))
    var h = when (max) {
        r -> (g - b) / delta % 6
        g -> (b - r) / delta + 2
        b -> (r - g) / delta + 4
        else -> 0f
    }

    h = (h * 60).let { if (it < 0) it + 360 else it }

    // Reduce saturation and lightness by 20%
    val newS = (s * 0.8f).coerceIn(0f, 1f)
    val newL = (l * 0.8f).coerceIn(0f, 1f)

    // Convert HSL back to RGB
    val c = (1 - kotlin.math.abs(2 * newL - 1)) * newS
    val x = c * (1 - kotlin.math.abs((h / 60) % 2 - 1))
    val m = newL - c / 2

    val (r1, g1, b1) = when {
        h in 0f..60f -> Triple(c, x, 0f)
        h in 60f..120f -> Triple(x, c, 0f)
        h in 120f..180f -> Triple(0f, c, x)
        h in 180f..240f -> Triple(0f, x, c)
        h in 240f..300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r1 + m).coerceIn(0f, 1f),
        green = (g1 + m).coerceIn(0f, 1f),
        blue = (b1 + m).coerceIn(0f, 1f),
        alpha = alpha
    )
}
