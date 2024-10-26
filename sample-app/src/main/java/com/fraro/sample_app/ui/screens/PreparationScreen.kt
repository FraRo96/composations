package com.fraro.sample_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.fraro.sample_app.data.ShapeCustomization
import com.fraro.sample_app.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreparationScreen(
    onSubmitClick: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: MainViewModel = ViewModelProvider(lifecycleOwner)[MainViewModel::class.java]

    var nVertices by remember { mutableStateOf(6) }
    var nShapes by remember { mutableStateOf(0) }
    var isDropDownExpanded by remember { mutableStateOf(false) }
    var itemPosition by remember { mutableStateOf(0) }
    var shape by remember { mutableStateOf(ShapeCustomization.POLYGON) }
    var speedSliderRange by remember { mutableStateOf(0f..100f) }
    var rotationSliderRange by remember { mutableStateOf(0f..360f) }
    var isClockwise by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PreparationScreenTitle()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SpeedSlider(
                    sliderPosition = speedSliderRange,
                    callback = { range ->
                        speedSliderRange = range
                    }
                )
                RotationSlider(
                    sliderPosition = rotationSliderRange,
                    isClockwise = isClockwise,
                    rangeCallback = { range ->
                        rotationSliderRange = range
                    },
                    directionCallback = { dir ->
                        isClockwise = dir
                    }
                )
                if (shape == ShapeCustomization.POLYGON) {
                    NumberVerticesSlider(nVertices)
                }
            }


            GeometricThumbnail(shape = shape, nVertices = nVertices)

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
}

@Composable
fun NumberVerticesSlider(nVertices: Int) {

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
            .padding(bottom = 5.dp)
    )
}

@Composable
fun SliderTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        //fontSize = 20.sp,
        modifier = Modifier
            .padding(top = 25.dp)
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
fun GeometricThumbnail(shape: ShapeCustomization, nVertices: Int) {
    /*Row(
        verticalAlignment = Alignment.CenterVertically
    ) {*/
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            //.fillMaxWidth()
            .background(Color.White)
            //.fillMaxHeight()
            //.background(Color.Red)
            .padding(top = 100.dp),
    ) {

            when (shape) {
                ShapeCustomization.POLYGON -> {
                    Box(
                        modifier = Modifier
                            .drawWithCache {
                                val roundedPolygon = RoundedPolygon(
                                    numVertices = nVertices,
                                    radius = 200F,
                                    centerX = 0F,
                                    centerY = 0F
                                )
                                val roundedPolygonPath = roundedPolygon
                                    .toPath()
                                    .asComposePath()
                                onDrawBehind {
                                    drawPath(roundedPolygonPath, color = Color.Blue)
                                }
                            }
                            .fillMaxHeight(1f)
                    )
                }

                else -> {}
            //}
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
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PreparationScreenTitle()

        Column() {
            Slider()
            Slider()
            Slider()
        }
        GeometricThumbnail(shape = ShapeCustomization.POLYGON, nVertices = 6)
    }
}
