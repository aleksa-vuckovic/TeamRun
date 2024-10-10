package com.example.runpal.server

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path


interface UploadApi {

    /**
     * @return The upload (photo) identified by id, as a byte array.
     */
    @GET("uploads/{id}")
    suspend fun get(@Path("id") id: String): ResponseBody
}