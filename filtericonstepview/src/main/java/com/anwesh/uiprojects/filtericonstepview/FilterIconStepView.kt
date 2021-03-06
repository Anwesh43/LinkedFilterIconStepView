package com.anwesh.uiprojects.filtericonstepview

/**
 * Created by anweshmishra on 18/04/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.content.Context
import android.app.Activity

val nodes : Int = 5
val lines : Int = 2
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#FF6F00")
val backColor : Int = Color.parseColor("#BDBDBD")
val rFactor : Float = 5.9f
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap
fun Int.sjf() : Float = 1f - 2 * (this % 2).toFloat()

fun Canvas.drawFilterIconStep(i : Int, xGap : Float, size : Float, sc : Float, paint : Paint) {
    val y : Float = i.sjf() * (-size * sc.divideScale(i, lines))
    save()
    translate(xGap * i, size/2 * i.sjf())
    drawLine(0f, 0f, 0f, y, paint)
    paint.style = Paint.Style.FILL
    drawCircle(0f, y, xGap / rFactor, paint)
    restore()
}

fun Canvas.drawFISNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.style = Paint.Style.STROKE
    val xGap : Float = (size) / (lines - 1)
    save()
    translate(w / 2, gap * (i + 1))
    rotate(90f * sc2)
    drawArc(RectF(-size, -size, size, size), 0f, 360f * sc1, false, paint)
    translate(-size / 2, 0f)
    for (j in 0..(lines - 1)) {
        drawFilterIconStep(j, xGap, size, sc1, paint)
    }
    restore()
}

class FilterIconStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class FISNode(var i : Int, val state : State = State()) {

        private var next : FISNode? = null
        private var prev : FISNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = FISNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawFISNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : FISNode {
            var curr : FISNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class FilterIconStep(var i : Int) {

        private val root : FISNode = FISNode(0)
        private var curr : FISNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : FilterIconStepView) {
        private val animator : Animator = Animator(view)
        private val fis : FilterIconStep = FilterIconStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            fis.draw(canvas, paint)
            animator.animate {
                fis.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            fis.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : FilterIconStepView {
            val view : FilterIconStepView = FilterIconStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}