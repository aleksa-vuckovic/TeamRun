package com.example.runpal

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.runpal.repositories.LoginManager
import com.example.runpal.repositories.run.CombinedRunRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@HiltWorker
class DailyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    val loginManager: LoginManager,
    val combinedRunRepository: CombinedRunRepository)
    : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                combinedRunRepository.attemptSyncAll()
                loginManager.refresh()
            } catch (_: Exception){

            }
            Result.success()
        }
    }
}