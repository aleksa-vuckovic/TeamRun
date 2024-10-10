package com.example.runpal.server

import com.example.runpal.models.Room
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * All of the API calls return a GenericResponse object.
 * If successful, 'message' is set to "ok", and 'data' holds the relevant data, if any.
 * (This is what @return refers to.)
 * If unsuccessful, 'message' contains the relevant error message.
 */
interface RoomApi {

    /**
     * Creates a new room.
     *
     * @return The new room _id.
     */
    @GET("room/create")
    suspend fun create(): GenericResponse<String>

    /**
     * Join a room. If the user has already joined, the call will return a success message,
     * and the state will remain unchanged. If the room has 5 or more members, it is considered 'full'.
     *
     * @param room The room _id.
     * @return Just the success message.
     */
    @GET("room/join/{room}")
    suspend fun join(@Path("room") room: String): GenericResponse<Unit>

    /**
     * Set the current user's state to ready.
     * Only succeeds if the user is already a member.
     *
     * @param room The room _id.
     * @return Just the success message.
     */
    @GET("room/ready/{room}")
    suspend fun ready(@Path("room") room: String): GenericResponse<Unit>

    /**
     * Leave room.
     * Only succeeds if user is not in ready state.
     * If the user is not a member, the call is ignored, but successful.
     *
     * @param room The room _id.
     * @return Just the success message.
     */
    @GET("room/leave/{room}")
    suspend fun leave(@Path("room") room: String): GenericResponse<Unit>

    /**
     * Get the current room state.
     *
     * @param room The room _id.
     * @return The Room object corresponding to the current room state.
     * Null if the room does not exist.
     */
    @GET("room/status/{room}")
    suspend fun status(@Path("room") room: String): GenericResponse<Room>
}