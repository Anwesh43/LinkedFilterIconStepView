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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.divideScale(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap
fun Int.sjf() : Float = 1f - 2 * (this % 2).toFloat()

fun Canvas.drawFilterIconStep(i : Int, xGap : Float, size : Float, sc : Float, paint : Paint) {
    val y : Float = i.sjf() * (-size * sc.divideScale(i, lines))
    save()
    translate(xGap * i, size/2 * i.sjf())
    drawLine(0f, 0f, 0f, y, paint)
    paint.style = Paint.Style.FILL
    drawCircle(0f, y, xGap / 5, paint)
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
    val xGap : Float = (2 * size) / (lines - 1)
    save()
    translate(w / 2, gap * (i + 1))
    rotate(90f * sc2)
    drawArc(RectF(-size, -size, size, size), 0f, 360f * sc1, false, paint)
    for (j in 0..(lines - 1)) {
        drawFilterIconStep(j, xGap, size, sc1, paint)
    }
    restore()
}

class FilterIconStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                
            }
        }
        return true
    }
}