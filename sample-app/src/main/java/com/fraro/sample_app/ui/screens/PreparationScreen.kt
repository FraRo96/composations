package com.fraro.sample_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.fraro.composable_realtime_animations.data.models.Shape
import com.fraro.sample_app.SimulationRoute
import com.fraro.sample_app.data.Rotation
import com.fraro.sample_app.data.ShapeCustomization
import com.fraro.sample_app.data.SimulationActor
import com.fraro.sample_app.data.SimulationModel
import com.fraro.sample_app.data.Trace
import com.fraro.sample_app.ui.viewmodels.MainViewModel

@Composable
fun PreparationScreen(
    onSubmitClick: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = context as ViewModelStoreOwner
    val viewModel: MainViewModel = ViewModelProvider(lifecycleOwner)[MainViewModel::class.java]

    LazyColumn {
        items(ShapeCustomization.values()) { shape ->
            ShapeCustomizationItem(shape)
        }
    }
    /*Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            onSubmitClick()
        }) {
            Text(text = "Go to simulation!")
        }
    } */
}

@Composable
fun ShapeCustomizationItem(shape: ShapeCustomization) {
    val nVertices = remember { mutableStateOf(6) }
    val nShapes = remember { mutableStateOf(0) }
    val isDropDownExpanded = remember { mutableStateOf(false) }
    val itemPosition = remember { mutableStateOf(0) }

    ElevatedCard(
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(10),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .background(Color.White, RoundedCornerShape(10))
                    .padding(30.dp)
                    .fillMaxHeight()
            ) {
                GeometricThumbnail(shape, nVertices.value)
            }
            if (shape == ShapeCustomization.POLYGON) {
                Column() {
                    IconButton(onClick = { if (nVertices.value < 20) nVertices.value++ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "add $shape",
                            tint = Color.Black.copy(alpha=0.45F)
                        )
                    }
                    IconButton(onClick = { if (nVertices.value > 3) nVertices.value-- }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "subtract $shape",
                            tint = Color.Black.copy(alpha=0.45F)
                        )
                    }
                }
            }
            Column() {
                IconButton(onClick = { if (nShapes.value < 10000) nShapes.value++ }) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "add $shape")
                }
                IconButton(onClick = { if (nShapes.value > 0) nShapes.value-- }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "subtract $shape")
                }
            }
            //Spacer(modifier = Modifier.fillMaxWidth(0.1f))
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .background(Color.White, RoundedCornerShape(30))
                    .padding(5.dp)
                    .fillMaxHeight()
            ) {
                Text(text = "${nShapes.value}")
            }

            Spacer(modifier = Modifier.fillMaxWidth(0.1f))
            Box {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        isDropDownExpanded.value = true
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
                    expanded = isDropDownExpanded.value,
                    onDismissRequest = {
                        isDropDownExpanded.value = false
                    }) {
                    Trace.values().forEachIndexed { index, trace ->
                        DropdownMenuItem(text = {
                            Text(text = trace.description)
                        },
                            onClick = {
                                isDropDownExpanded.value = false
                                itemPosition.value = index
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun GeometricThumbnail(shape: ShapeCustomization, nVertices: Int) {
    when (shape) {
        ShapeCustomization.POLYGON -> {
            Box(
                modifier = Modifier
                    .drawWithCache {
                        val roundedPolygon = RoundedPolygon(
                            numVertices = nVertices,
                            radius = 50F,
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
    }
}
