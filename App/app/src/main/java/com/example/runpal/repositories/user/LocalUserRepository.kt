package com.example.runpal.repositories.user

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.example.runpal.NotFound
import com.example.runpal.makePermanentFile
import com.example.runpal.models.User
import com.example.runpal.room.UserDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalUserRepository @Inject constructor(private val userDao: UserDao, @ApplicationContext val context: Context):
    UserRepository {

    private suspend fun saveProfile(user: User) {
        try { //delete previous profile picture
            val previous = getUser(user._id)
            previous.profileUri.toFile().delete()
        } catch (_: Exception) {}

        val profileFile = user.profileUri.toFile()
        val permanentFile = context.makePermanentFile(profileFile)
        user.profileUri = Uri.fromFile(permanentFile)
    }

    /**
     * Update user data in the local Room database.
     * If the profile field URI does not point to the files directory of the application,
     * a copy of the file is placed there, and the inserted entity contains the URI pointing to the new copy.
     */
    override suspend fun update(user: User) {
        saveProfile(user)
        userDao.update(user)
    }

    /**
     * Same as update, but inserts if does not exist yet.
     */
    override suspend fun upsert(user: User) {
        saveProfile(user)
        userDao.upsert(user)
    }


    override suspend fun getUser(id: String): User {
        val result = userDao.get(id)
        if (result == null) throw NotFound("User does not exist.")
        else return result
    }
}