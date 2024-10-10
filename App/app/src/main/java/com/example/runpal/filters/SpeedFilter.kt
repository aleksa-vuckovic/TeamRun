package com.example.runpal.filters

import com.example.runpal.models.PathPoint


/**
 * @param minSpan Minimum time span in milliseconds over which to average the speed.
 */
class SpeedFilter(
    private val minSpan: Long = 1000L
) {
    private var start: PathPoint? = null
    private val path: MutableList<PathPoint> = mutableListOf()


    fun filter(cur: PathPoint): Double {
        if (start == null) {
            start = cur
            return 2.7
        }
        while (!path.isEmpty() && (cur.time - path.first().time) >= minSpan) start = path.removeFirst()
        path.add(cur)
        val timeDiff = cur.time - start!!.time
        if (timeDiff == 0L) return cur.speed
        else return (cur.distance - start!!.distance)/timeDiff.toDouble()*1000.0
    }
    fun clear(start: PathPoint? = null) {
        if (start?.isInit() == false) this.start = start
        path.clear()
    }
}