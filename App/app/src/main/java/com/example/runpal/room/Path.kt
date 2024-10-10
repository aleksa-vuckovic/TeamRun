package com.example.runpal.room

import androidx.room.Entity
import com.example.runpal.models.PathPoint

/**
 * This entity represents a path point within a running session.
 * Corresponds to the PathPoint class, with the runId and user fields
 * as the only addition.
 */
@Entity(tableName = "path", primaryKeys = ["user", "runId", "time"])
class Path(
    val user: String = "",
    val runId: Long = 0L,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val time: Long = 0L,
    val end: Boolean = false,
    val speed: Double = 0.0,
    val distance: Double = 0.0,
    val kcal: Double = 0.0
) {

}

fun PathPoint.toPath(user: String, runId: Long): Path = Path(user = user, runId = runId, latitude = latitude, longitude = longitude, altitude = altitude,
    time = time, end = end, speed = speed, distance = distance, kcal = kcal)
fun Path.toPathPoint(): PathPoint = PathPoint(latitude, longitude, altitude, time, end, speed, distance, kcal)