package com.fraro.composable_realtime_animations.data.models

import androidx.compose.ui.graphics.Path
import androidx.graphics.shapes.RoundedPolygon

sealed class Shape(val description: String, open val size: Size = Size.RescaleFactor(1F)) {
    data object Unspecified : Shape("Unspecified")
    data class Segment(override val size: Size.DoubleAxisMeasure = Size.DoubleAxisMeasure(10F, 1F))
        : Shape("Segment")
    data class Rectangle(override val size: Size.DoubleAxisMeasure = Size.DoubleAxisMeasure(10F, 10F))
        : Shape("Rectangle")
    data class RegularPolygon(
        val nVertices: Int,
        override val size: Size.SingleAxisMeasure = Size.SingleAxisMeasure(10F)
    ) : Shape("Regular polygon")

    data class Ellipse(override val size: Size.DoubleAxisMeasure = Size.DoubleAxisMeasure(100F, 100F))
        : Shape("Ellipse")
    data class CustomPolygonalShape(
        val roundedPolygon: RoundedPolygon? = null,
        val path: Path,
        override val size: Size.RescaleFactor = Size.RescaleFactor(100F)
    ) : Shape("Custom path")

    companion object {
        fun listShapes(scaleFactor: Float, customPath: Path): List<Shape> {
            return listOf(
                Unspecified,
                Segment(
                    size = Size.DoubleAxisMeasure(scaleFactor, 1f)
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
    data class DoubleAxisMeasure(val height: Float, val width: Float): Size
    data class SingleAxisMeasure(val size: Float): Size
    data class RescaleFactor(val scale: Float): Size
}