package com.example.runpal.models

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = false)
    var _id: String = "",
    var email: String = "",
    var name: String = "",
    var last: String = "",
    /**
     * This field's content depends on the context:
     * 1. In the local Room database, it contains the profile image URI.
     * 2. On the server, it contains a unique name of the image file.
     * 3. At runtime, when user data is retrieved from the server by the server user repository,
     *      it will contain the URI of a temporary profile image file, stored in the cache directory.
     */
    var profile: String = "",
    var weight: Double = 0.0
) {

    var profileUri: Uri
        get() = Uri.parse(profile)
        set(uri) {
            profile = uri.toString()
        }
}