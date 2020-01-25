package com.maksimstepanov.coloredtextview

import android.content.Context
import android.graphics.*
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
    private var needsReinitialization = true
    private var pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        pathEffect = CornerPathEffect(30f)
    }
    private var path = Path()

    override fun onDraw(canvas: Canvas?) {
        if (needsReinitialization) {
            boundsArray = createRects(layout.lineCount)
            needsReinitialization = false
        }
        for (i in 0 until layout.lineCount) {
            val bounds = boundsArray[i]
            layout.getLineBounds(i, bounds)
            bounds.top = layout.getLineTop(i) - paddingTop
            bounds.left = layout.getLineLeft(i).toInt() - paddingLeft
            bounds.right =
                layout.getLineLeft(i).toInt() + layout.getLineWidth(i).toInt() + paddingRight
            bounds.bottom = layout.getLineBottom(i) + paddingBottom
        }

        canvas?.save()
        canvas?.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        for (rect in boundsArray) {
            path.reset()
            createPath(path, boundsArray, 0)
            canvas?.drawPath(path, pathPaint)
        }
        layout.paint.color = Color.WHITE
        layout.draw(canvas)
        canvas?.restore()
    }

    private fun createRects(lineCount: Int): Array<Rect> {
        if (lineCount < 1) {
            return emptyArray()
        }
        return Array(lineCount) { Rect() }
    }

    private fun createPath(path: Path, arr: Array<Rect>, i: Int): Int {
        val rect = arr[i]
        if (i == 0) {
            path.moveTo(rect.left.toFloat(), rect.top.toFloat())
            path.lineTo(rect.right.toFloat(), rect.top.toFloat())
            if (arr.size > i + 1) {
                val nextRect = arr[i + 1]
                if (nextRect.right > rect.right) {
                    path.rLineTo(0f, nextRect.top.toFloat() - rect.top.toFloat())
                    path.lineTo(nextRect.right.toFloat(), nextRect.top.toFloat())
                } else {
                    path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
                    path.rLineTo(-(rect.right.toFloat() - nextRect.right.toFloat()), 0f)
                }
            } else {
                path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
            }
        }
        if (i == arr.size - 1) {
            path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
            path.lineTo(rect.left.toFloat(), rect.bottom.toFloat())
            if (i - 1 >= 0) {
                val prevRect = arr[i - 1]
                if (prevRect.left.toFloat() < rect.left.toFloat()) {
                    path.rLineTo(0f, -(rect.bottom.toFloat() - prevRect.bottom.toFloat()))
                    path.lineTo(prevRect.left.toFloat(), prevRect.bottom.toFloat())
                } else {
                    path.lineTo(rect.left.toFloat(), rect.top.toFloat())
                    path.rLineTo(prevRect.left.toFloat() - rect.left.toFloat(), 0f)
                }
            } else {
                path.lineTo(rect.left.toFloat(), rect.top.toFloat())
            }
            return i - 1
        }

        if (i != 0 && i != arr.size - 1) {
            val nextRect = arr[i + 1]
            if (nextRect.right > rect.right) {
                path.rLineTo(0f, nextRect.top.toFloat() - rect.top.toFloat())
                path.lineTo(nextRect.right.toFloat(), nextRect.top.toFloat())
            } else {
                path.lineTo(rect.right.toFloat(), rect.bottom.toFloat())
                path.rLineTo(-(rect.right.toFloat() - nextRect.right.toFloat()), 0f)
            }
        }

        val previous = createPath(path, arr, i + 1)

        val prevRect = arr[previous]
        if (prevRect.left.toFloat() < rect.left.toFloat()) {
            path.rLineTo(0f, -(rect.bottom.toFloat() - prevRect.bottom.toFloat()))
            path.lineTo(prevRect.left.toFloat(), prevRect.bottom.toFloat())
        } else {
            path.lineTo(rect.left.toFloat(), rect.top.toFloat())
            path.rLineTo(prevRect.left.toFloat() - rect.left.toFloat(), 0f)
        }
        return previous - 1
    }
}