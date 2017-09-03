package net.redwarp.app.multitool.compass

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import net.redwarp.app.multitool.R

class CompassView : View {
    var angle: Float = 0f
        set(value) {
            field = value % 360f
            invalidate()
        }
    private val angleAnimator: ValueAnimator = ValueAnimator()

    private val paint = Paint()
    private val textPaint = Paint()
    private val northArrowPaint = Paint()
    private val southArrowPaint = Paint()
    private val north: Direction by lazy {
        Direction(context.getString(R.string.direction_north), 0f)
    }
    private val east: Direction by lazy {
        Direction(context.getString(R.string.direction_east), 90f)
    }
    private val south: Direction by lazy {
        Direction(context.getString(R.string.direction_south), 180f)
    }
    private val west: Direction by lazy {
        Direction(context.getString(R.string.direction_west), 270f)
    }
    private val arrow = Arrow()

    constructor(context: Context) : super(context) {
        commonInit(null)
    }

    constructor(context: Context,
                attrs: AttributeSet?) : super(context, attrs) {
        commonInit(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        commonInit(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        commonInit(attrs)
    }

    private fun commonInit(attrs: AttributeSet?) {
        val obtainStyledAttributes = context.obtainStyledAttributes(attrs, intArrayOf(R.attr.colorPrimary, R.attr.colorPrimaryLight))

        paint.color = obtainStyledAttributes.getColor(1, Color.WHITE)
        paint.isAntiAlias = true
        textPaint.color = obtainStyledAttributes.getColor(0, Color.BLACK)
        textPaint.isAntiAlias = true
        northArrowPaint.color = Color.RED
        northArrowPaint.isAntiAlias = true
        southArrowPaint.color = obtainStyledAttributes.getColor(0, Color.BLACK)
        southArrowPaint.isAntiAlias = true

        obtainStyledAttributes.recycle()
        angleAnimator.addUpdateListener {
            this.angle = it.animatedValue as Float
        }
        angleAnimator.interpolator = LinearInterpolator()
    }

    /**
     * A compass view should be square.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Update letters
        val width = w.toFloat()
        textPaint.textSize = width / 6f
        north.measureOffsets(textPaint, width)
        east.measureOffsets(textPaint, width)
        south.measureOffsets(textPaint, width)
        west.measureOffsets(textPaint, width)
        arrow.measure(width)
    }

    override fun draw(canvas: Canvas?) {
        if (canvas != null) {
            canvas.save()
            canvas.translate(width / 2f, height / 2f)
            canvas.drawCircle(0f, 0f, width / 2f, paint)
            north.draw(canvas, textPaint)
            east.draw(canvas, textPaint)
            south.draw(canvas, textPaint)
            west.draw(canvas, textPaint)
            arrow.draw(canvas)
            canvas.restore()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (angleAnimator.isRunning) {
            angleAnimator.end()
        }
    }

    fun setAngle(angle: Float, animated: Boolean) {
        if (animated) {
            angleAnimator.cancel()
            val (fromAngle, toAngle) = findAngles(this.angle, angle)
            angleAnimator.setFloatValues(fromAngle, toAngle)
            angleAnimator.start()
        } else {
            this.angle = angle
        }
    }

    private fun findAngles(angle1: Float, angle2: Float): Pair<Float, Float> {
        if (angle1 > angle2) {
            if (Math.abs(angle2 - angle1) < Math.abs(angle2 + 360f - angle1)) {
                return Pair(angle1, angle2)
            } else {
                return Pair(angle1, angle2 + 360f)
            }
        } else {
            if (Math.abs(angle2 - angle1) < Math.abs(angle2 - 360f - angle1)) {
                return Pair(angle1, angle2)
            } else {
                return Pair(angle1, angle2 - 360f)
            }
        }
    }

    private class Direction(val text: String, val degrees: Float) {
        var xOffset = 0f
        var yOffset = 0f

        fun measureOffsets(paint: Paint, width: Float) {
            xOffset = -paint.measureText(text) / 2f
            yOffset = -width * .3f
        }

        fun draw(canvas: Canvas, paint: Paint) {
            canvas.save()

            canvas.rotate(degrees, 0f, 0f)
            canvas.drawText(text, xOffset, yOffset, paint)

            canvas.restore()
        }
    }

    private inner class Arrow {
        val path = Path()
        val workRect = RectF()

        fun measure(width: Float) {
            path.rewind()
            path.moveTo(-width / 20f, 0f)
            path.lineTo(0f, -width * .4f)
            path.lineTo(width / 20f, 0f)
            workRect.set(-width / 20f, -width / 20f, width / 20f, width / 20f)
            path.arcTo(workRect, 0f, 180f, true)
            path.close()
            path.fillType = Path.FillType.EVEN_ODD
        }

        fun draw(canvas: Canvas) {
            canvas.save()
            canvas.rotate(180f + angle)
            canvas.drawPath(path, southArrowPaint)
            canvas.rotate(180f)
            canvas.drawPath(path, northArrowPaint)
            canvas.restore()
            canvas.drawCircle(0f, 0f, width / 32f, southArrowPaint)
        }
    }
}
