package com.fraro.composable_realtime_animations.data.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.graphics.shapes.CornerRounding

data class ParticleVisualizationModel(
    val id: Long,
    val screenPosition: ScreenPosition,
    val duration: Int,
    val shape: Shape = Shape.Unspecified,
    val color: Color? = null,
    val bitmap: ImageBitmap? = null,
)


data class ScreenPosition(
    val offset: Offset,
    val heading: Float
)

sealed interface Shape {
    object Unspecified : Shape
    data class Segment(val size: Size.SingleAxisMeasure? = null) : Shape
    data class Rectangle(val size: Size.DoubleAxisMeasure? = null) : Shape
    data class RegularPolygon(
        val nVertices: Int,
        val size: Size.SingleAxisMeasure? = null
    ) : Shape
    data class Ellipse(val size: Size.DoubleAxisMeasure? = null) : Shape
    data class CustomPolygonalShape(
        val path: Path,
        val size: Size.RescaleFactor? = null
    ) : Shape
}

sealed interface Size {
    data class DoubleAxisMeasure(val height: Float, val width: Float)
    data class SingleAxisMeasure(val size: Float)
    data class RescaleFactor(val scale: Float)
}