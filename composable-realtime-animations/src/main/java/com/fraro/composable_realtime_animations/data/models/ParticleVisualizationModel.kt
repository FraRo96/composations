package com.fraro.composable_realtime_animations.data.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path

data class ParticleVisualizationModel(
    val id: Long,
    val screenPosition: ScreenPosition,
    val duration: Int,
    val delayFactor: Float = 0F,
    val directionUnitVector: Offset? = null, //useful only if isForward is true in canvas
    val shape: Shape = Shape.Unspecified,
    val color: Color? = null,
    val bitmap: ImageBitmap? = null,
)

data class ScreenPosition(
    val offset: Offset,
    val heading: Float
)

sealed class Shape(val description: String) {
    object Unspecified : Shape("Unspecified")
    data class Segment(val size: Size.SingleAxisMeasure = Size.SingleAxisMeasure(1F)) : Shape("Segment")
    data class Rectangle(val size: Size.DoubleAxisMeasure = Size.DoubleAxisMeasure(10F, 10F)) : Shape("Rectangle")
    data class RegularPolygon(
        val nVertices: Int,
        val size: Size.SingleAxisMeasure = Size.SingleAxisMeasure(10F)
    ) : Shape("Regular polygon")

    data class Ellipse(val size: Size.DoubleAxisMeasure = Size.DoubleAxisMeasure(100F, 100F)) : Shape("Ellipse")
    data class CustomPolygonalShape(
        val path: Path,
        val size: Size.RescaleFactor = Size.RescaleFactor(100F)
    ) : Shape("Custom path")

    companion object {
        fun listShapes(scaleFactor: Float, customPath: Path): List<Shape> {
            return listOf(
                Unspecified,
                Segment(
                    size = Size.SingleAxisMeasure(scaleFactor)
                ),
                Rectangle(
                    size = Size.DoubleAxisMeasure(scaleFactor, scaleFactor)
                ),
                RegularPolygon(
                    nVertices = 6,
                    size = Size.SingleAxisMeasure(scaleFactor)
                ), // Example with 5 vertices
                Ellipse(
                    size = Size.DoubleAxisMeasure(scaleFactor, scaleFactor)
                ),
                CustomPolygonalShape(
                    path = customPath,
                    size = Size.RescaleFactor(scaleFactor)
                ) // Assuming default Path() is empty
            )
        }
    }
}

sealed interface Size {
    data class DoubleAxisMeasure(val height: Float, val width: Float)
    data class SingleAxisMeasure(val size: Float)
    data class RescaleFactor(val scale: Float)
}