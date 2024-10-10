package com.example.runpal.models


/**
 * Represents a group run room.
 *
 * @param _id The room _id.
 * @param members Emails of member users.
 * @param ready Users who are ready to start.
 * (Must only include values from the members set)
 * @param start The UNIX timestamp start time,
 * set by the server when all members are ready.
 */
data class Room(
    val _id: String = "",
    val members: List<String> = listOf(),
    val ready: List<String> = listOf(),
    val start: Long? = null
) {
}