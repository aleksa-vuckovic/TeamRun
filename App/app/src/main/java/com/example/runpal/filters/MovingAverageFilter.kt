package com.example.runpal.filters

class MovingAverageFilter(private val size: Int) {

    private val data: Array<Double> = Array(size) {0.0}
    private var i = 0
    private var full: Boolean = false
    private var avg = 0.0

    fun filter(x: Double): Double {
        if (!full) {
            avg = (avg*i + x) / (i+1)
            data[i++] = x

            i %= size
            if (i == 0) full = true
        }
        else {
            avg = avg * size - data[i] + x
            avg /= size

            data[i++] = x
            i %= size
        }
        return avg
    }

}