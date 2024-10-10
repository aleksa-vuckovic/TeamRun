package com.example.runpal.repositories.user

import com.example.runpal.models.User

/**
 * All methods will throw an exception upon failure.
 */
interface UserRepository {

    /**
     * Updates the user with the new data.
     * The profile field is expected to be a valid URI,
     * referencing an image file on the device.
     */
    suspend fun update(user: User)

    /**
     * Same as update, but if a user with the given email does not exist,
     * a new one is inserted. Not supported on the server.
     */
    suspend fun upsert(user: User)

    /**
     * Retrieves user with given email.
     */
    suspend fun getUser(id:String): User
}