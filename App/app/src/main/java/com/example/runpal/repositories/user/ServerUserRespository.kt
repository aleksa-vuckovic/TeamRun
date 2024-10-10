package com.example.runpal.repositories.user

import android.content.Context
import com.example.runpal.ServerException
import com.example.runpal.getBitmap
import com.example.runpal.models.User
import com.example.runpal.repositories.LoginManager
import com.example.runpal.repositories.ServerUploadRepository
import com.example.runpal.server.UserApi
import com.example.runpal.toMultipartPart
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerUserRespository @Inject constructor(
    private val userApi: UserApi,
    private val loginManager: LoginManager,
    @ApplicationContext private val context: Context,
    private val serverUploadRepository: ServerUploadRepository
): UserRepository {


    override suspend fun update(user: User) {
        val response = userApi.update(user.name, user.last, user.weight.toString(), user.profileUri.getBitmap(context.contentResolver)?.toMultipartPart("profile"))
        if (response.message != "ok") throw ServerException(response.message)
        loginManager.setToken(response.data)
    }

    /**
     * Unsupported - The only way to add a new user on the server is through registration.
     */
    override suspend fun upsert(user: User) {
        throw Exception("Upserts not supported on the server side!")
    }

    /**
     * The profile picture will be retrieved, saved in the cache directory,
     * and the profile field will be set to the corresponding URI.
     */
    override suspend fun getUser(id: String): User {
        val response = userApi.data(id)
        if (response.message != "ok") throw ServerException(response.message)
        val user = response.data!!
        user.profileUri = serverUploadRepository.get(user.profile)
        return user
    }
}