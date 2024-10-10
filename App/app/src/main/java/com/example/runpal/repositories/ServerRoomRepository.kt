package com.example.runpal.repositories

import com.example.runpal.ServerException
import com.example.runpal.models.Room
import com.example.runpal.server.RoomApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * All methods till throw an exception upon failure.
 */
@Singleton
class ServerRoomRepository @Inject constructor(private val roomApi: RoomApi){

    /**
     * Create room.
     *
     * @return The room _id.
     */
    suspend fun create(): String {
        val response = roomApi.create()
        if (response.message != "ok") throw ServerException(response.message)
        return response.data!!
    }

    /**
     * Join a room. If the user has already joined, the call will return a success message,
     * and the state will remain unchanged. If the room has 5 or more members, it is considered 'full'.
     *
     * @param room The room _id.
     */
    suspend fun join(room: String) {
        val response = roomApi.join(room)
        if (response.message != "ok") throw ServerException(response.message)
    }

    /**
     * Set the current user's state to ready.
     * Only succeeds if the user is already a member.
     *
     * @param room The room _id.
     */
    suspend fun ready(room: String) {
        val response = roomApi.ready(room)
        if (response.message != "ok") throw ServerException(response.message)
    }

    /**
     * Leave room.
     * Only succeeds if user is not in ready state.
     * If the user is not a member, the call is ignored, but successful.
     *
     * @param room The room _id.
     */
    suspend fun leave(room: String) {
        val response = roomApi.leave(room)
        if (response.message != "ok") throw ServerException(response.message)
    }

    /**
     * Get the current room state. Throws exception if the room does not exist.
     *
     * @param room The room _id.
     * @return The Room object corresponding to the current room state.
     */
    suspend fun status(room: String): Room {
        val response = roomApi.status(room)
        if (response.message != "ok") throw ServerException(response.message)
        return response.data!!
    }
}