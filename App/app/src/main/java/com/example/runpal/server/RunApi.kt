package com.example.runpal.server

import com.example.runpal.models.Run
import com.example.runpal.models.RunData
import com.google.gson.reflect.TypeToken
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RunApi {

    @POST("run/create")
    suspend fun create(@Body run: Run): GenericResponse<Unit>

    @POST("run/update")
    suspend fun update(@Body runData: RunData): GenericResponse<Unit>

    @GET("run/getupdate")
    suspend fun getUpdate(@Query("user") user: String, @Query("id") id: Long? = null,
                          @Query("room") room: String? = null, @Query("event") event: String? = null,
                          @Query("since") since: Long = 0) : GenericResponse<RunData>

    @GET("run/all")
    suspend fun getAll(@Query("until") until: Long, @Query("limit") limit: Int): GenericResponse<List<RunData>>

    @GET("run/since")
    suspend fun getSince(@Query("since") since: Long): GenericResponse<List<RunData>>

    @GET("run/unfinished")
    suspend fun unfinished(): GenericResponse<RunData>

    @GET("run/delete/{id}")
    suspend fun delete(@Path("id") id: Long): GenericResponse<Unit>
}