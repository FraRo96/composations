package com.fraro.composable_realtime_animations.data.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

interface Element<T> {
    val id: Long
    fun getData(): MutableMap<Int, Value<T>>
    val initialValue: T
}

interface DelayedElement<T> : Element<T> {
    val duration: Long
    val delayFactor: Float
    val isConstant: Boolean
}

interface VectorElement<T> : Element<T> {
    val directionVector: Pair<Float, Float>
}

class DelayedElementDecorator<T>(
    override val id: Long,
    override val duration: Long,
    override val delayFactor: Float,
    override val isConstant: Boolean,
    override val initialValue: T,
    private val wrappedElement: Element<T>
) : DelayedElement<T> {

    override fun getData(): MutableMap<Int, Value<T>> {
        return wrappedElement.getData()
    }
}

class VectorElementDecorator<T>(
    override val id: Long,
    override val directionVector: Pair<Float, Float>,
    override val initialValue: T,
    private val wrappedElement: Element<T>
) : VectorElement<T> {

    override fun getData(): MutableMap<Int, Value<T>> {
        return wrappedElement.getData()
    }
}

data class ScreenPosition(
    val offset: Offset,
    val heading: Float
)

sealed class Shape(val description: String) {
    data object Unspecified : Shape("Unspecified")
    data class Segment(val size: Size.DoubleAxisMeasure = Size.DoubleAxisMeasure(10F, 1F)) : Shape("Segment")
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