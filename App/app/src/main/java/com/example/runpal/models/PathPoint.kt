package com.example.runpal.models

import android.location.Location
import com.example.runpal.EARTH_RADIUS_M
import com.example.runpal.Point3D
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Represents a single point along the path of a running activity.
 * Includes the basic info: lat, lon, alt, time.
 * Also includes the current speed, distance and kcal.
 *
 * @property end Indicates whether this path point is the end of
 * a segment (which happens when the user pauses)
 * or possibly of the entire run (when the user ends the session).
 * @property time UNIX timestamp.
 */
data class PathPoint(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    //UNIX timestamp ??Might not be valid if the location provider changes??
    val time: Long = 0L,
    //indicates whether this path point is the end of a segment (which happens when the user pauses)
    //or possibly of the entire run (when the user ends the session)
    var end: Boolean = false,
    //speed at the time
    var speed: Double = 0.0,
    //cumulative distance
    var distance: Double = 0.0,
    //cumulative calories spent
    var kcal: Double = 0.0,

    ) {

    /**
     * Calculates the distance in meters between two path points,
     * according to the Haversine formula, which approximates the Earth
     * as a sphere.
     *
     * @return Distance in meters.
     */
    fun distance(to: PathPoint): Double {
        return this.toLatLng().distance(to.toLatLng())
    }

    companion object {
        /**
         * Used as the initial location in a run state.
         * (No location data, and zero distance,sped,kcal).
         * NOTE: The location data in an init path point is invalid, HOWEVER the distance and kcal data are.
         */
        val INIT = PathPoint()

        /**
         * Use when recovering a run state, so the path point does represent an unknown location,
         * but still holds the accumulated run data.
         */
        fun init(distance: Double, kcal: Double) = PathPoint(distance = distance, kcal = kcal)
    }
    fun isInit(): Boolean = time == 0L

}

fun Location.toPathPoint(): PathPoint = PathPoint(latitude, longitude, altitude, time, false)
fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)
fun PathPoint.toLatLng(): LatLng = LatLng(latitude, longitude)
fun LatLng.toPoint3D(): Point3D {
    val z = Math.sin(Math.toRadians(latitude))
    val x = Math.cos(Math.toRadians(longitude))
    val y = Math.sin(Math.toRadians(longitude))
    return Point3D(x* EARTH_RADIUS_M ,y* EARTH_RADIUS_M, z* EARTH_RADIUS_M)
}

/**
 * The distance between to points in meters.
 */
fun LatLng.distance(to: LatLng): Double {
    val lon1Rad = Math.toRadians(longitude)
    val lat1Rad = Math.toRadians(latitude)
    val lon2Rad = Math.toRadians(to.longitude)
    val lat2Rad = Math.toRadians(to.latitude)

    val dlon = lon2Rad - lon1Rad
    val dlat = lat2Rad - lat1Rad

    val a = sin(dlat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dlon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    // Radius of the Earth in meters
    val radiusEarth = 6371000.0
    return radiusEarth * c
}
fun List<LatLng>.toDistance(): Double {
    var result = 0.0
    for (i in 1..size-1)
        result += this[i-1].distance(this[i])
    return result
}