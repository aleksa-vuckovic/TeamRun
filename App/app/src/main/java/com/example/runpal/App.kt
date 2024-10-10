package com.example.runpal

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class App: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, {
            Log.d("RENDERER CHECK", it.toString())
        })

        //Event reminders
        val reminderIntent = Intent(this, EventReminderReceiver::class.java)
        reminderIntent.action = ACTION_DAILY_REMINDER
        var reminder = PendingIntent.getBroadcast(this, REMINDER_REQUEST_CODE, reminderIntent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (reminder == null) {
            reminder = PendingIntent.getBroadcast(this, REMINDER_REQUEST_CODE, reminderIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAtMillis = System.currentTimeMillis() + 3600000L
            alarmManager.setRepeating(AlarmManager.RTC, triggerAtMillis, 24*3600000L, reminder)
        }

        val workManager = WorkManager.getInstance(this)
        val existingWorkPolicy = ExistingPeriodicWorkPolicy.KEEP

        val workRequest = PeriodicWorkRequestBuilder<DailyWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            existingWorkPolicy,
            workRequest
        )
    }
}