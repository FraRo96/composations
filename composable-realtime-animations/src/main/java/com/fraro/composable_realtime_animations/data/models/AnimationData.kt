package com.fraro.composable_realtime_animations.data.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

/*data class PastOffsetAnimationElement(
    override val id: Long,
    override val shape: Shape,
    override val color: Color,
    override val screenPosition: ScreenPosition,
    override val imageBitmap: ImageBitmap? = null,
    val duration: Int,
    val delayFactor: Float = 0F,
) : AnimationElement

data class FutureOffsetAnimationElement(
    override val id: Long,
    override val shape: Shape,
    override val color: Color,
    override val screenPosition: ScreenPosition,
    override val imageBitmap: ImageBitmap? = null,
    val directionUnitVector: Offset
): AnimationElement */

interface AbstractElement {
    val id: Long
}

interface Element<T, U> : AbstractElement {
    fun getData(): U
    val initialValue: T
}

interface DelayedElement<T, U> : Element<T, U> {
    val duration: Long
    val delayFactor: Float
    val isConstant: Boolean
}

interface VectorElement<T, U> : Element<T, U> {
    val directionVector: Pair<Float, Float>
}

class DelayedElementDecorator<T, U, V>(
    override val id: Long,
    override val duration: Long,
    override val delayFactor: Float,
    override val isConstant: Boolean,
    override val initialValue: T,
    private val wrappedElement: Element<V, U>
) : DelayedElement<T, U> {

    override fun getData(): U {
        return wrappedElement.getData()
    }
}

class VectorElementDecorator<T, U, V>(
    override val id: Long,
    override val directionVector: Pair<Float, Float>,
    override val initialValue: T,
    private val wrappedElement: Element<V, U>
) : VectorElement<T, U> {

    override fun getData(): U {
        return wrappedElement.getData()
    }
}

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