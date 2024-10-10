package com.example.runpal.server

import com.example.runpal.models.User
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * All of the API calls return a GenericResponse object.
 * If successful, 'message' is set to "ok", and 'data' holds the relevant data, if any.
 * (This is what @return refers to.)
 * If unsuccessful, 'message' contains the relevant error message.
 */
interface UserApi {

    /**
     * @return Profile data for the specified user.
     * The profile field is the server upload id of the photo.
     */
    @GET("user/data")
    suspend fun data(@Query("id") id: String): GenericResponse<User>

    /**
     * Update user data.
     * Fields set to null will be unchanged.
     * Note that the JWT token should be refreshed after this, since the payload data has most likely changed.
     *
     * @return Just the success message.
     */
    @Multipart
    @POST("user/update")
    suspend fun update(@Part("name") name: String?,
               @Part("last") last: String?,
               @Part("weight") weight: String?,
               @Part profile: MultipartBody.Part?): GenericResponse<String>
}