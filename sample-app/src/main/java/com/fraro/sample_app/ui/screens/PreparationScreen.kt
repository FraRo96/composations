package com.fraro.sample_app.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.composable_realtime_animations.data.models.Size
import com.fraro.sample_app.data.Trace
import com.fraro.sample_app.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreparationScreen(
    onSubmitClick: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: MainViewModel = ViewModelProvider(lifecycleOwner)[MainViewModel::class.java]

    var nVertices = remember { mutableStateMapOf(Pair(0L, 6)) }
    var nShapes = remember { mutableStateMapOf(Pair(0L, 6)) }
    val isTraceDropDownExpanded = remember { mutableStateMapOf(Pair(0L, false)) }
    val isShapeDropDownExpanded = remember { mutableStateMapOf(Pair(0L, false)) }
    val trace = remember { mutableStateMapOf(Pair(0L, Trace.DIAGONAL)) }
    val shape = remember { mutableStateMapOf<Long, Shape>(Pair(0L, Shape.RegularPolygon(
        6, Size.SingleAxisMeasure(200F)))) }
    val speedSliderRange = remember { mutableStateMapOf(Pair(0L, 0f..100f)) }
    val sizeSliderRange = remember { mutableStateMapOf(Pair(0L, 1f..100f)) }
    val sizeSliderSingle = remember { mutableStateMapOf(Pair(0L, 1f)) }
    val rotationSliderRange = remember { mutableStateMapOf(Pair(0L, 0f..360f)) }
    val isClockwise = remember { mutableStateMapOf(Pair(0L, true)) }
    var currentParticle by remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PreparationScreenTitle()

        GeometricThumbnail(
            shape = shape[currentParticle]!!,
            changeNVerticesCallback = { delta ->
                val polygon = shape[currentParticle]!! as Shape.RegularPolygon
                if (delta == 1 && polygon.nVertices < 20) {

                    shape[currentParticle] = Shape.RegularPolygon(
                        nVertices = (shape[currentParticle]!! as Shape.RegularPolygon).nVertices + 1,
                        size = polygon.size
                    )
                } else if (delta == -1 && polygon.nVertices > 3) {

                    shape[currentParticle] = Shape.RegularPolygon(
                        nVertices = polygon.nVertices - 1,
                        size = polygon.size
                    )
                }
            }
        )

        Row(
        ) {
            OutlinedButton(onClick = {
                currentParticle--
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    contentDescription = "Go to previous particle"
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Previous")
            }
            Spacer(modifier = Modifier.weight(0.1f))
            OutlinedButton(onClick = {
                currentParticle++
            }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    contentDescription = "Clear particle"
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Clear")
            }
            Spacer(modifier = Modifier.weight(0.1f))
            OutlinedButton(onClick = {
                currentParticle++
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    contentDescription = "Go to next particle"
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Next")
            }
        }
        Spacer(modifier = Modifier.height(5.dp))

        SpeedSlider(
            sliderPosition = speedSliderRange[currentParticle]!!,
            callback = { range ->
                speedSliderRange[currentParticle] = range
            }
        )
        Spacer(modifier = Modifier.height(25.dp))
        RotationSlider(
            sliderPosition = rotationSliderRange[currentParticle]!!,
            isClockwise = isClockwise[currentParticle]!!,
            rangeCallback = { range ->
                rotationSliderRange[currentParticle] = range
            },
            directionCallback = { dir ->
                isClockwise[currentParticle] = dir
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        SizeSlider(
            rangeSliderPosition = sizeSliderRange[currentParticle]!!,
            singleSliderPosition = sizeSliderSingle[currentParticle]!!,
            rangeCallback = { range -> sizeSliderRange[currentParticle] = range },
            singleCallback = { value -> sizeSliderSingle[currentParticle] = value },
            shape = shape[currentParticle]!!
        )
        Spacer(modifier = Modifier.height(30.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Shape: ",
                fontWeight = FontWeight.Bold
            )
            Box(
                //modifier = Modifier.padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.clickable {
                        isShapeDropDownExpanded[currentParticle] = true
                    }
                ) {
                    Text(text = shape[currentParticle]!!.description)
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown of shapes",
                        //modifier = Modifier.background(Color.White, RoundedCornerShape(70))
                    )
                }
                DropdownMenu(
                    expanded = isShapeDropDownExpanded[currentParticle]!!,
                    onDismissRequest = {
                        isShapeDropDownExpanded[currentParticle] = false
                    }
                ) {
                    Shape.getAllShapes(200F).forEachIndexed { _, shapeValue ->
                        if (!(shapeValue is Shape.Unspecified)) {

                            DropdownMenuItem(
                                text = {
                                    Text(text = shapeValue.description)
                                },
                                onClick = {
                                    isShapeDropDownExpanded[currentParticle] = false
                                    shape[currentParticle] = shapeValue
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(0.1f))
            Text(
                text = "Trace: ",
                fontWeight = FontWeight.Bold
            )
            Box(
                //modifier = Modifier.padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.clickable {
                        isTraceDropDownExpanded[currentParticle] = true
                    }
                ) {
                    Text(text = trace[currentParticle]!!.description)
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown of traces",
                        //modifier = Modifier.background(Color.White, RoundedCornerShape(70))
                    )
                }
                DropdownMenu(
                    expanded = isTraceDropDownExpanded[currentParticle]!!,
                    onDismissRequest = {
                        isTraceDropDownExpanded[currentParticle] = false
                    }
                ) {
                    Trace.values().forEachIndexed { index, traceValue ->
                        DropdownMenuItem(
                            text = {
                                Text(text = traceValue.description)
                            },
                            onClick = {
                                isTraceDropDownExpanded[currentParticle] = false
                                trace[currentParticle] = traceValue
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = { /*TODO*/ },
            modifier = Modifier.padding(top = 25.dp)
        ) {
            Text(text = "Start animation!")
        }
    }
}
        /*Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    //.align(Alignment.CenterVertically)
                    .background(Color.White, RoundedCornerShape(10))
                    .padding(30.dp)
                    .fillMaxHeight()
            ) {
                GeometricThumbnail(shape, nVertices)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { if (nShapes < 10000) nShapes++ }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "add")
                }
                Column(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(30))
                        //.padding(5.dp)
                        .fillMaxHeight()
                ) {
                    Text(text = "${nShapes}")
                }
                IconButton(onClick = { if (nShapes > 0) nShapes-- }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "subtract")
                }
            }

            //Spacer(modifier = Modifier.fillMaxWidth(0.1f))
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "Movement", fontWeight = FontWeight.Bold)
                Box(
                    //modifier = Modifier.padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.clickable {
                            isDropDownExpanded = true
                        }
                    ) {
                        Text(text = Trace.BORDERS.description)
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "dropdown of traces",
                            //modifier = Modifier.background(Color.White, RoundedCornerShape(70))
                        )
                    }
                    DropdownMenu(
                        expanded = isDropDownExpanded,
                        onDismissRequest = {
                            isDropDownExpanded = false
                        }) {
                        Trace.values().forEachIndexed { index, trace ->
                            DropdownMenuItem(text = {
                                Text(text = trace.description)
                            },
                                onClick = {
                                    isDropDownExpanded = false
                                    itemPosition = index
                                })
                        }
                    }
                }
                Row() {
                    CompositionLocalProvider(
                        LocalMinimumTouchTargetEnforcement provides false,
                    ) {
                        IconButton(
                            onClick = { if (nVertices > 0) nVertices-- },
                            modifier = Modifier.then(Modifier.size(40.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "clear",
                                tint = Color.Black.copy(alpha = 0.45F)
                            )
                        }
                        IconButton(
                            onClick = { if (nVertices < 20) nVertices++ },
                            modifier = Modifier.then(Modifier.size(40.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "add",
                                tint = Color.Black.copy(alpha = 0.45F)
                            )
                        }
                    }
                }
            }
        } */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeSlider(
    rangeSliderPosition: ClosedFloatingPointRange<Float>,
    singleSliderPosition: Float,
    rangeCallback: (ClosedFloatingPointRange<Float>) -> Unit,
    singleCallback: (Float) -> Unit,
    shape: Shape
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (shape) {
            is Shape.RegularPolygon, is Shape.Segment, is Shape.CustomPolygonalShape -> {

                SliderTitle(text = "Size")
                Slider(
                    value = singleSliderPosition,
                    //steps = 1000,
                    onValueChange = { value -> singleCallback(value) },
                    valueRange = 1f..100f,
                    onValueChangeFinished = {
                        // launch some business logic update with the state you hold
                        // viewModel.updateSelectedSliderValue(sliderPosition)
                    },
                )
                SliderSubText(
                    text = singleSliderPosition.toString(),
                )
            }

            else -> {

                SliderTitle(text = "Length - Height")
                RangeSlider(
                    value = rangeSliderPosition,
                    //steps = 1000,
                    onValueChange = { range -> rangeCallback(range) },
                    valueRange = 1f..100f,
                    onValueChangeFinished = {
                        // launch some business logic update with the state you hold
                        // viewModel.updateSelectedSliderValue(sliderPosition)
                    },
                )
                SliderSubText(
                    text = rangeSliderPosition.toString(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedSlider(
    sliderPosition: ClosedFloatingPointRange<Float>,
    callback: (ClosedFloatingPointRange<Float>) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SliderTitle(text = "Speed (dp/s)")
        RangeSlider(
            value = sliderPosition,
            //steps = 1000,
            onValueChange = { range -> callback(range) },
            valueRange = 1f..100f,
            onValueChangeFinished = {
                // launch some business logic update with the state you hold
                // viewModel.updateSelectedSliderValue(sliderPosition)
            },
        )
        SliderSubText(
            text = sliderPosition.toString(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationSlider(
    sliderPosition: ClosedFloatingPointRange<Float>,
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
                    //steps = 1000,
                    onValueChange = { range -> rangeCallback(range) },
                    valueRange = 1f..100f,
                    onValueChangeFinished = {
                        // launch some business logic update with the state you hold
                        // viewModel.updateSelectedSliderValue(sliderPosition)
                    },
                )
                Checkbox(checked = isClockwise, onCheckedChange = { dir -> directionCallback(dir) })
            }
            SliderSubText(
                text = sliderPosition.toString(),
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
        text = "Set up simulation",
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
fun GeometricThumbnail(shape: Shape, changeNVerticesCallback: (Int) -> Unit) {
    /*Row(
        verticalAlignment = Alignment.CenterVertically
    ) {*/
    when (shape) {
        is Shape.RegularPolygon -> {
            val halfSizeDp = (shape.size!!.size / 2F).dp
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
                                val roundedPolygonPath = roundedPolygon
                                    .toPath()
                                    .asComposePath()
                                onDrawBehind {
                                    drawPath(roundedPolygonPath, color = Color.Blue)
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
        else -> {

        }
    }
}

@Composable
fun Slider() {
    var sliderPosition by remember {
        mutableFloatStateOf(0f)
    }
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            steps = 3,
            valueRange = 0f..50f
        )
        Text(text = sliderPosition.toString())
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreparationScreenPolygonPreview() {

    Column(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PreparationScreenTitle()

        Column() {
            Slider()
            Slider()
            Slider()
        }
        //GeometricThumbnail(shape = shape[current])
    }
}
