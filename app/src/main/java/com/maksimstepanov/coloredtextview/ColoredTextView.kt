package com.maksimstepanov.coloredtextview

import android.content.Context
import android.graphics.*
import android.text.BoringLayout
import android.text.Layout
import android.text.StaticLayout
import android.util.AttributeSet
import android.widget.TextView

/**
 *
 *
 * @author Maksim Stepanov on 24.01.2020
 */
class ColoredTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.style.AppTheme
) : TextView(context, attrs, defStyle) {

    private var boundsArray = emptyArray<Rect>()
    private var pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        pathEffect = CornerPathEffect(context.resources.getDimension(R.dimen.corner_radius))
    }
    private var path = Path()
    private var needsReinitialization = true

    override fun onDraw(canvas: Canvas?) {
        if (needsReinitialization) {
            boundsArray = createRects(layout.lineCount)
        }

        for (i in 0 until layout.lineCount) {
            val bounds = boundsArray[i]
            layout.getLineBounds(i, bounds)
            bounds.top = layout.getLineTop(i) - (paddingTop - 10)
            bounds.left = layout.getLineLeft(i).toInt() - (paddingLeft - 10)
            bounds.right =
                layout.getLineLeft(i).toInt() + layout.getLineWidth(i).toInt() + (paddingRight - 10)
            bounds.bottom = layout.getLineBottom(i) + (paddingBottom - 10)
        }

        canvas?.save()
        canvas?.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        for (rect in boundsArray) {
            path.reset()
            createPath(path, boundsArray)
            canvas?.drawPath(path, pathPaint)
        }
        canvas?.restore()

        super.onDraw(canvas)
    }

    private fun createRects(lineCount: Int): Array<Rect> {
        if (lineCount < 1) {
            return emptyArray()
        }
        return Array(lineCount) { Rect() }
    }

    private fun createPath(path: Path, arr: Array<Rect>) {
        val count = arr.size
        when {
            count <= 0 -> return
            count == 1 -> {
                val rect = arr[0]
                singleLine(rect, path)
            }
            else -> multipleLines(arr, path)
        }
    }

    private fun singleLine(rect: Rect, path: Path) {
        val rLeft = rect.left.toFloat()
        val rTop = rect.top.toFloat()
        val rRight = rect.right.toFloat()
        val rBottom = rect.bottom.toFloat()
        path.moveTo(rLeft, rTop)
        path.lineTo(rRight, rTop)
        path.lineTo(rRight, rBottom)
        path.lineTo(rLeft, rBottom)
        path.close()
    }

    private fun multipleLines(arr: Array<Rect>, path: Path) {
        recursiveHelper(arr, path, 0)
    }

    private fun recursiveHelper(arr: Array<Rect>, path: Path, i: Int) {
        val rect = arr[i]
        val rLeft = rect.left.toFloat()
        val rTop = rect.top.toFloat()
        val rRight = rect.right.toFloat()
        val rBottom = rect.bottom.toFloat()

        if (i == 0) {
            val nextRect = arr[i + 1]
            val nextTop = nextRect.top.toFloat()
            val nextRight = nextRect.right.toFloat()

            path.moveTo(rLeft, rTop)
            path.lineTo(rRight, rTop)
            if (nextRight > rRight) {
                path.rLineTo(0f, nextTop - rTop)
                path.lineTo(nextRight, nextTop)
            } else {
                path.lineTo(rRight, rBottom)
                path.rLineTo(-(rRight - nextRight), 0f)
            }
        } else if (i == arr.size - 1) {
            val prevRect = arr[i - 1]
            val prevLeft = prevRect.left.toFloat()
            val prevBottom = prevRect.bottom.toFloat()

            path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
            path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
            if (prevLeft < rLeft) {
                path.rLineTo(0f, -(rBottom - prevBottom))
                path.lineTo(prevLeft, prevBottom)
            } else {
                path.lineTo(rLeft, rTop)
                path.rLineTo(prevLeft - rLeft, 0f)
            }
            return
        } else {
            val nextRect = arr[i + 1]
            val nextRight = nextRect.right.toFloat()
            val nextTop = nextRect.top.toFloat()

            if (nextRect.right > rect.right) {
                path.rLineTo(0f, nextTop - rTop)
                path.lineTo(nextRight, nextTop)
            } else {
                path.lineTo(rRight, rBottom)
                path.rLineTo(-(rRight - nextRight), 0f)
            }
        }

        recursiveHelper(arr, path, i + 1)

        if (i == 0) {
            path.close()
        } else {
            val prevRect = arr[i - 1]
            val prevLeft = prevRect.left.toFloat()
            val prevBottom = prevRect.bottom.toFloat()

            if (prevLeft < rLeft) {
                path.rLineTo(0f, -(rBottom - prevBottom))
                path.lineTo(prevLeft, prevBottom)
            } else {
                path.lineTo(rLeft, rTop)
                path.rLineTo(prevLeft - rLeft, 0f)
            }
        }

        return
    }
}