package com.example.runpal.repositories

import android.content.Context
import android.net.Uri
import com.example.runpal.ServerException
import com.example.runpal.getBitmap
import com.example.runpal.models.Event
import com.example.runpal.models.EventResult
import com.example.runpal.server.EventApi
import com.example.runpal.server.GenericResponse
import com.example.runpal.toMultipartPart
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

/**
 * All methods till throw an exception upon failure.
 */
@Singleton
class ServerEventRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventApi: EventApi,
    private val serverUploadRepository: ServerUploadRepository
) {

    /**
     * Create a new event.
     * Name and time must be specified.
     *
     * @return The created event _id.
     */
    suspend fun create(name: String, description: String? = null, time: Long, distance: Double, image: Uri? = null, path: List<LatLng>?, tolerance: Double?): String {
        val response = eventApi.create(
            name = name,
            description = description,
            time = time,
            distance = distance,
            image = image?.getBitmap(context.contentResolver)?.toMultipartPart(fieldName = "image", fileName = "image.png"),
            path = if (path == null) null else Gson().toJson(path, object : TypeToken<List<LatLng>>() {}.type),
            tolerance = tolerance
        )
        if (response.message != "ok") throw ServerException(response.message)
        else return response.data!!
    }


    /**
     * Get data for the event with given identifier.
     */
    suspend fun data(event: String): Event {
        val response = eventApi.data(event)
        if (response.message != "ok") throw ServerException(response.message)
        val data = response.data!!
        data.imageUri = serverUploadRepository.get(data.image)
        return data
    }

    /**
     *  Find upcoming events, using the given criteria.
     *
     *  @return A list of matched events, sorted by time ascending.
     */
    @GET("event/find")
    suspend fun find(search: String? = null,
                     following: Boolean? = null
    ): List<Event> {
        val response = eventApi.find(search = search, following = following)
        if (response.message != "ok") throw ServerException(response.message)
        val res = response.data!!
        for (event in res) event.imageUri = serverUploadRepository.get(event.image)
        return res
    }

    /**
     * Follow the event.
     */
    suspend fun follow(event: String) {
        val response = eventApi.follow(event)
        if (response.message != "ok") throw ServerException(response.message)
    }

    /**
     * Unfollow event.
     */
    suspend fun unfollow(event: String) {
        val response = eventApi.unfollow(event)
        if (response.message != "ok") throw ServerException(response.message)
    }

    /**
     * Returns the entire list of results for users who have finished the race,
     * sorted from best. Only returns users who have finished.
     */
    suspend fun ranking(event: String): List<EventResult> {
        val response = eventApi.ranking(event)
        if (response.message != "ok") throw ServerException(response.message)
        return response.data!!
    }

    /**
     * Returns the live results of an event, limited to 10 entries.
     * First entries are users who have finished the race, sorted by time,
     * and second are users who are still running, sorted by distance.
     */
    suspend fun rankingLive(event: String): List<EventResult> {
        val response = eventApi.rankingLive(event)
        if (response.message != "ok") throw ServerException(response.message)
        return response.data!!
    }


}