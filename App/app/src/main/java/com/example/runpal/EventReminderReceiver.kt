package com.example.runpal

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.runpal.activities.home.HomeActivity
import com.example.runpal.repositories.ServerEventRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EventReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var eventRepository: ServerEventRepository

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {

        if (!context.hasNotificationPermission()) return
        val manager = NotificationManagerCompat.from(context)
        val channel = NotificationChannelCompat.Builder(REMINDER_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
            .setName("Event reminders")
            .setDescription("Notifications presented a few hours or days before a followed event.")
            .build()
        manager.createNotificationChannel(channel)


        CoroutineScope(Dispatchers.Default).launch {
            try {
                val events = eventRepository.find(following = true)
                if (!events.isEmpty())  {
                    val next = events[0]
                    val till = next.time - System.currentTimeMillis()
                    val time = LongTimeFormatter.format(till).first
                    val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                    if (till > 28*3600000L) return@launch
                    if (till < 3600000L) notification.setContentText("${next.name} is starting in just ${time}. Are you ready?")
                    else notification.setContentText("Have you prepared for ${next.name}? You only have ${time} left.")
                    notification.setContentTitle(next.name)
                    notification.setSmallIcon(R.drawable.runner)

                    val deepLinkIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(EVENT_DEEP_LINK_URI + next._id),
                        context,
                        HomeActivity::class.java
                    )
                    val deepLinkPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
                        addNextIntentWithParentStack(deepLinkIntent)
                        getPendingIntent(EVENT_VIEW_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    }
                    notification.setContentIntent(deepLinkPendingIntent)
                    manager.notify(REMINDER_NOTIFICATION_ID, notification.build())
                }
            } catch(e: Exception) {e.printStackTrace()}
        }
    }
}