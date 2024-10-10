package com.example.runpal

class Point3D(val x: Double, val y: Double, val z: Double) {

    operator fun plus(it: Point3D): Point3D = Point3D(x + it.x, y + it.y, z + it.z)
    operator fun minus(it: Point3D): Point3D = Point3D(x - it.x, y - it.y, z - it.z)
    operator fun times(it: Point3D): Double = x*it.x + y*it.y + z*it.z
    operator fun times(it: Double): Point3D = Point3D(x*it, y*it, z*it)
    operator fun div(it: Double): Point3D = this * (1/it)
    fun norm2(): Double = this*this
    fun norm(): Double = Math.sqrt(norm2())
}