package com.amir.test

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.Toast
import java.util.Vector

/**
 * Created by Fernando on 15/8/30.
 */
class Playground(context: Context?) : SurfaceView(context), OnTouchListener {
    private val matrix: Array<Array<Dot?>>
    private var cat: Dot? = null
    private fun getDot(x: Int, y: Int): Dot? {
        return matrix[y][x]
    }

    private fun isAtEdge(d: Dot?): Boolean {
        return if (d!!.x * d.y == 0 || d.x + 1 == COL || d.y + 1 == ROW) {
            true
        } else false
    }

    private fun getNeighbor(one: Dot?, dir: Int): Dot? {
        when (dir) {
            1 -> return getDot(one!!.x - 1, one.y)
            2 -> return if (one!!.y % 2 == 0) {
                getDot(one.x - 1, one.y - 1)
            } else {
                getDot(one.x, one.y - 1)
            }
            3 -> return if (one!!.y % 2 == 0) {
                getDot(one.x, one.y - 1)
            } else {
                getDot(one.x + 1, one.y - 1)
            }
            4 -> return getDot(one!!.x + 1, one.y)
            5 -> return if (one!!.y % 2 == 0) {
                getDot(one.x, one.y + 1)
            } else {
                getDot(one.x + 1, one.y + 1)
            }
            6 -> return if (one!!.y % 2 == 0) {
                getDot(one.x - 1, one.y + 1)
            } else {
                getDot(one.x, one.y + 1)
            }
            else -> {}
        }
        return null //?
    }

    private fun getDistance(one: Dot?, dir: Int): Int { //checks the status of cat's neighbor dot(neighbor dot)
        var distance = 0
        if (isAtEdge(one)) {
            return 1
        }
        var ori = one
        var next: Dot?
        while (true) {
            next = getNeighbor(ori, dir)
            if (next!!.status == Dot.STATUS_ON) {
                return distance * -1
            }
            if (isAtEdge(next)) {
                distance++
                return distance
            }
            distance++
            ori = next
        }
    }

    private fun MoveTo(one: Dot?) { //moves the cat to the new position on the game board (one is the next dot)
        one!!.status = Dot.STATUS_IN
        getDot(cat!!.x, cat!!.y)!!.status = Dot.STATUS_OFF
        cat!!.setXY(one.x, one.y)
    }

    private fun move() { //?
        if (isAtEdge(cat)) {
            lose()
            return
        }
        val avaliable = Vector<Dot?>()
        val positive = Vector<Dot?>()
        val al = HashMap<Dot?, Int>()
        for (i in 1..6) {
            val n = getNeighbor(cat, i)
            if (n!!.status == Dot.STATUS_OFF) {
                avaliable.add(n)
                al[n] = i
                if (getDistance(n, i) > 0) {
                    positive.add(n)
                }
            }
        }
        if (avaliable.size == 0) {
            win()
        } else if (avaliable.size == 1) {
            MoveTo(avaliable[0])
        } else {
            var best: Dot? = null
            if (positive.size != 0) { //Free direction exists
                println("Step forward")
                var min = 999
                for (i in positive.indices) {
                    val a = getDistance(positive[i], al[positive[i]]!!)
                    if (a < min) {
                        min = a
                        best = positive[i]
                    }
                }
            } else { //All have blocks
                println("Avoid Blocks")
                var max = 0
                for (i in avaliable.indices) {
                    val k = getDistance(avaliable[i], al[avaliable[i]]!!)
                    if (k <= max) {
                        max = k
                        best = avaliable[i]
                    }
                }
            }
            MoveTo(best)
        }
    }

    private fun lose() {
        Toast.makeText(context, "Lose", Toast.LENGTH_SHORT).show()
    }

    private fun win() {
        Toast.makeText(context, "You win", Toast.LENGTH_SHORT).show()
    }

    private fun redraw() {
        val c = holder.lockCanvas()
        c.drawColor(Color.LTGRAY)
        val paint = Paint()
        paint.flags = Paint.ANTI_ALIAS_FLAG
        for (i in 0 until ROW) {
            var offset = 0
            if (i % 2 != 0) {
                offset = WIDTH / 2
            }
            for (j in 0 until COL) {
                val one = getDot(j, i)
                when (one!!.status) {
                    Dot.STATUS_OFF -> paint.color = -0x111112
                    Dot.STATUS_ON -> paint.color = -0x5600
                    Dot.STATUS_IN -> paint.color = -0x10000
                    else -> {}
                }
                c.drawOval(
                    RectF(
                        (one.x * WIDTH + offset).toFloat(),
                        (one.y * WIDTH).toFloat(),
                        ((one.x + 1) * WIDTH + offset).toFloat(),
                        ((one.y + 1) * WIDTH).toFloat()
                    ), paint
                )
            }
        }
        holder.unlockCanvasAndPost(c)
    }

    var callback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            redraw()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            WIDTH = width / (COL + 1)
            redraw()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {}
    }

    init {
        holder.addCallback(callback)
        matrix = Array(ROW) { arrayOfNulls(COL) }
        for (i in 0 until ROW) {
            for (j in 0 until COL) {
                matrix[i][j] = Dot(j, i)
            }
        }
        setOnTouchListener(this)
        initGame()
    }

    private fun initGame() {
        for (i in 0 until ROW) {
            for (j in 0 until COL) {
                matrix[i][j]!!.status = Dot.STATUS_OFF
            }
        }
        cat = Dot(4, 5)
        getDot(4, 5)!!.status = Dot.STATUS_IN
        var i = 0
        while (i < BLOCKS) {
            val x = (Math.random() * 1000 % COL).toInt()
            val y = (Math.random() * 1000 % ROW).toInt()
            if (getDot(x, y)!!.status == Dot.STATUS_OFF) {
                getDot(x, y)!!.status = Dot.STATUS_ON
                i++
                println("Block: $i")
            }
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
//            Toast.makeText(getContext(),event.getX()+":"+ event.getY(),Toast.LENGTH_SHORT).show();
            val x: Int
            val y: Int
            y = event.y.toInt() / WIDTH
            x = if (y % 2 == 0) {
                (event.x / WIDTH).toInt()
            } else {
                ((event.x - WIDTH / 2) / WIDTH).toInt()
            }
            if (x + 1 > COL || y + 1 > ROW) {
                initGame()
                //                System.out.println("----------------------------");
//                for (int i = 1;i<7;i++){
//                    System.out.println(i+"@"+getDistance(cat,i));
//                }
            } else if (getDot(x, y)!!.status == Dot.STATUS_OFF) {
                getDot(x, y)!!.status = Dot.STATUS_ON
                move()
            }
            redraw()
        }
        return true
    }

    companion object {
        private var WIDTH = 40
        private const val COL = 10
        private const val ROW = 10
        private const val BLOCKS = 15 //default roadblock numbers 默认路障数量
    }
}