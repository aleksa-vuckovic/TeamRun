package com.example.runpal.models

/**
 * Represents one entry in the event results table.
 * Exactly one of 'time' and 'distance' is not null.
 *
 * @param time Time from event start to finish, or null if user has not finished yet.
 * @param distance Current running distance, or null if user has finished.
 */
class EventResult(
    val user: String = "",
    val name: String = "",
    val last: String = "",
    val time: Long? = null,
    val distance: Double? = null,
    val disqualified: Boolean? = null
) {

    val finished: Boolean
        get() = time != null && disqualified != null
    val running: Boolean
        get() = distance != null

    override fun toString(): String {
        return "[$name $last ($user), $last $time $distance]"
    }
}