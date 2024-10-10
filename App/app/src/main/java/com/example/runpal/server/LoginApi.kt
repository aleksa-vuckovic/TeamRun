package com.example.runpal.server

import com.example.runpal.models.User
import okhttp3.MultipartBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

class LoginResponse(
    val token: String,
    val user: User
)

/**
 * All of the API calls return a GenericResponse object.
 * If successful, 'message' is set to "ok", and 'data' holds the relevant data, if any.
 * (This is what @return refers to.)
 * If unsuccessful, 'message' contains the relevant error message.
 */
interface LoginApi {

    @GET("test")
    suspend fun test(@Query("test") testData: String): GenericResponse<Unit>

    /**
     * Login using email and password.
     *
     * @return JWT token.
     */
    @FormUrlEncoded
    @POST("login")
    suspend fun login(@Field("email") email: String, @Field("password") password: String): GenericResponse<LoginResponse>

    /**
     * Refresh a valid JWT token.
     *
     * @return New JWT token.
     */
    @GET("refresh")
    suspend fun refresh(@Header("Authorization") auth: String = "Bearer "): GenericResponse<String>

    /**
     * Submits the user data for registration.
     *
     * @return JWT token.
     */
    @Multipart
    @POST("register")
    suspend fun register(@Part("email") email: String,
                         @Part("password") password: String,
                         @Part("name") name: String,
                         @Part("last") last: String,
                         @Part("weight") weight: String,
                         @Part profile: MultipartBody.Part?): GenericResponse<LoginResponse>
}

