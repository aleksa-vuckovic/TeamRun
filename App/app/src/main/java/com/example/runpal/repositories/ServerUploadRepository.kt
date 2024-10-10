package com.example.runpal.repositories

import android.content.Context
import android.net.Uri
import com.example.runpal.server.UploadApi
import com.example.runpal.tempServerFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Retrieves upload files from the server, stores them in the cache directory,
 * and keeps track of them, to reduce the number of requests sent to the server.
 */
@Singleton
class ServerUploadRepository @Inject constructor(private val uploadApi: UploadApi, @ApplicationContext private val context: Context) {

    private val cache: MutableMap<String, Uri> = mutableMapOf()

    suspend fun get(id: String): Uri {
        if (cache.containsKey(id)) return cache.get(id)!!
        val uploadBA = uploadApi.get(id).bytes()
        val tempFile = context.tempServerFile(id)

        withContext(Dispatchers.IO) {
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(tempFile)
                fos.write(uploadBA)
            } finally {
                fos?.close()
            }
        }
        val uri = Uri.fromFile(tempFile)
        cache.put(id, uri)
        return uri
    }
}