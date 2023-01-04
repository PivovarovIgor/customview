package ru.brauer.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.absoluteValue

class SomeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var startMovePoint: PointF? = null
    private val offsetMove: PointF = PointF(0f, 0f)

    private var paintLine: Paint = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeWidth = 1.dp.toFloat()
        style = Paint.Style.FILL
    }

    private var mCountOfLines: Int = 0
    var countOfLines: Int
        get() = mCountOfLines
        set(value) {
            mCountOfLines = value
            invalidate()
            requestLayout()
        }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.SomeView, defStyleAttr, 0).apply {
            try {
                mCountOfLines = getInteger(R.styleable.SomeView_countOfLines, 0)
                paintLine = Paint(ANTI_ALIAS_FLAG).apply {
                    color = getColor(R.styleable.SomeView_cellColor, Color.RED)
                    strokeWidth = getDimension(R.styleable.SomeView_cellWidthStroke, 1.dp.toFloat())
                    style = Paint.Style.FILL
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        println("VVV onAttachedToWindow")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val heightF = height.toFloat()
        val widthF = width.toFloat()
        val spaceH = heightF / countOfLines
        val spaceW = widthF / countOfLines
        repeat(countOfLines) {
            val yPos = spaceH * it + (offsetMove.y % spaceH)
            canvas?.drawLine(0f, yPos, widthF, yPos, paintLine)
            val xPos = spaceW * it + (offsetMove.x % spaceW)
            canvas?.drawLine(xPos, 0f, xPos, heightF, paintLine)
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        println("VVV dispatchDraw")
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        println("VVV draw")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val contentWidth = 100.dp
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val width = when(MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> contentWidth.also { println("VVV width UNSPECIFIED") }
            MeasureSpec.EXACTLY -> widthSpecSize.also { println("VVV width EXACTLY") }
            MeasureSpec.AT_MOST -> contentWidth.coerceAtMost(widthSpecSize).also { println("VVV width AT_MOST") }
            else -> error("Unreachable")
        }

        val contentHeight = 100.dp
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val height = when(MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> contentHeight.also { println("VVV height UNSPECIFIED") }
            MeasureSpec.EXACTLY -> heightSpecSize.also { println("VVV height EXACTLY") }
            MeasureSpec.AT_MOST -> contentHeight.coerceAtMost(heightSpecSize).also { println("VVV height AT_MOST") }
            else -> error("Unreachable")
        }
        setMeasuredDimension(width, height)
        println("VVV onMeasure widthMeasureSpec = $widthMeasureSpec; heightMeasureSpec = $heightMeasureSpec")
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        super.layout(l, t, r, b)
        println("VVV layout")
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        println("VVV onLayout")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        println("VVV onSizeChanged w = $w; h = $h; oldw = $oldw; oldh = $oldh")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        println("VVV onTouchEvent event = $event")
        return if (event.pointerCount == 1) processMove(event) else false
    }

    private fun processMove(event: MotionEvent): Boolean =
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startMovePoint = PointF(event.x, event.y)
                true
            }
            MotionEvent.ACTION_UP -> {
                startMovePoint = null
                true
            }
            MotionEvent.ACTION_MOVE -> {
                startMovePoint?.let {
                    val offsetX = offsetMove.x
                        .plus(event.x)
                        .minus(it.x)
                        .let {
                            if (it < 0) {
                                width - (it % width)
                            } else if (it > width) {
                                it % width
                            } else {
                                it
                            }
                        }
                    val offsetY = offsetMove.y
                        .plus(event.y)
                        .minus(it.y)
                        .let {
                            if (it < 0) {
                                height - (it % height)
                            } else if (it > height) {
                                it % height
                            } else {
                                it
                            }
                        }
                    offsetMove.set(offsetX, offsetY)
                    it.set(event.x, event.y)
                    invalidate()
                    true
                } ?: false
            }
            else -> false
        }.also { println("XXX offset ${offsetMove.x} ${offsetMove.y} $event") }
}

val Int.dp: Int get() = times(Resources.getSystem().displayMetrics.density).toInt()