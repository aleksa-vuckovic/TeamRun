package com.example.runpal.repositories

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.runpal.ServerException
import com.example.runpal.getBitmap
import com.example.runpal.server.LoginApi
import com.example.runpal.server.UserApi
import com.example.runpal.toMultipartPart
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class manages user JWT tokens,
 * user login, and logout.
 */
@Singleton
class LoginManager @Inject constructor(
    private val loginApi: LoginApi,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val SHARED_PREFS_LOGIN = "LOGIN"
        private const val SHARED_PREFS_LOGIN_EMAIL = "EMAIL"
        private const val SHARED_PREFS_LOGIN_ID = "ID"
        private const val SHARED_PREFS_LOGIN_TOKEN = "TOKEN"
        private const  val SHARED_PREFS_LOGIN_REFRESH = "REFRESH"
        private const val TOKEN_DURATION = 3*24*60*60*1000L
    }
    private fun getPrefs(): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFS_LOGIN, Context.MODE_PRIVATE)
    }
    /**
     * Returns the ID of the currently logged in user
     * or null if none is.
     */
    fun currentUserId(): String? {
        return getPrefs().getString(SHARED_PREFS_LOGIN_ID, null)
    }
    fun currentToken(): String? {
        return getPrefs().getString(SHARED_PREFS_LOGIN_TOKEN, null)
    }
    fun currentRefresh(): Long {
        return getPrefs().getLong(SHARED_PREFS_LOGIN_REFRESH, 0)
    }

    private fun setUserId(id: String?) {
        getPrefs().edit().putString(SHARED_PREFS_LOGIN_ID, id).apply()
    }
    internal fun setToken(token: String?) {
        getPrefs().edit().putString(SHARED_PREFS_LOGIN_TOKEN, token)
            .putLong(SHARED_PREFS_LOGIN_REFRESH, if (token == null) 0 else System.currentTimeMillis())
            .apply()
    }
    /**
     * Attempts to refresh the jwt token, if it exists.
     * Throws exception if not successful.
     */
    suspend fun refresh() {
        val user = currentUserId()
        val token = currentToken()
        if (user == null || token == null) throw Exception("No log history.")

        val response = loginApi.refresh(auth = "Bearer ${token}")
        if (response.message != "ok") throw ServerException(response.message)

        setToken(response.data)
    }

    /**
     * Attempts to register a new user on the server.
     * If successful, the user is immediately logged in.
     * Otherwise an exception is thrown.
     */
    suspend fun register(email: String, password: String, name: String, last: String, weight: Double, profile: Uri?) {
        val response = loginApi.register(email, password, name, last, weight.toString(), profile?.getBitmap(context.contentResolver)?.toMultipartPart(fieldName = "profile", fileName = "profile.png"))
        if (response.message != "ok") throw ServerException(response.message)

        setUserId(response.data!!.user._id)
        setToken(response.data!!.token)
    }

    /**
     * Logs the user out by deleting the JWT token.
     */
    fun logout() {
        setUserId(null)
        setToken(null)
    }

    /**
     * Attempts to log in using the given email and password.
     * Throws exception if not successful.
     */
    suspend fun login(email: String, password: String) {
        val response = loginApi.login(email, password)
        if (response.message != "ok") throw ServerException(response.message)

        setUserId(response.data!!.user._id)
        setToken(response.data!!.token)
    }

    fun logged(): Boolean {
        return System.currentTimeMillis() - currentRefresh() < TOKEN_DURATION
    }
}