package com.example.runpal.models

import androidx.room.Entity

/**
 * This model represents a running session, and is used in the room database.
 * On the server database, this object is part of a larger document,
 * containing also the location object and path array (like a RunUpdate object).
 *
 * @property id An identifier which must uniquely
 * identify a run for this user (probably a timestamp).
 * @property user The email of the user.
 * @property event The event identifier, if this run
 * is part of an event, else null.
 * @property room The room id, if this run is part
 * of a group running session, else null.
 * @property start The start timestamp, null if run did not start.
 * @property running Total running milliseconds.
 * @property end The end timestamp, null if run did not end (properly).
 * @property paused True if the run is currently paused, and only valid while end==null&&start!=null.
 * @property cur Used in events, represents the last reached event path point, to ensure that the user passes all points on the route.
 */
@Entity(tableName = "runs", primaryKeys = ["user", "id"])
data class Run(
    val user: String = "",
    val id: Long = UNKNOWN_ID,
    val event: String? = null,
    val room: String? = null,
    val start: Long? = null,
    val running: Long = 0L,
    val end: Long? = null,
    val paused: Boolean = false,
    val cur: Int? = null,
    val penalty: Double? = null
) {
    companion object {
        const val UNKNOWN_ID: Long = -1L
        val LOADING: Run = Run(running = -1L)
    }

    enum class State {
        LOADING,
        READY,
        RUNNING,
        PAUSED,
        ENDED
    }
    val state: State
        get() {
            if (this === LOADING) return State.LOADING
            else if (start == null) return State.READY
            else if (end != null) return State.ENDED
            else if (paused) return State.PAUSED
            else return State.RUNNING
        }
}