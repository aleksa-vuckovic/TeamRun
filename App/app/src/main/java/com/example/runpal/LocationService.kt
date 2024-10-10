package com.example.runpal

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import com.example.runpal.filters.LocationFilter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : LifecycleService() {

    @Inject
    @ApplicationContext lateinit var context: Context

    private val locationFilter: LocationFilter
    private var provider: FusedLocationProviderClient? = null
    init {
        val posBuff = 2000/LOCATION_UPDATE_PERIOD.toInt()
        locationFilter = LocationFilter(posBuff = posBuff, altBuff = posBuff*5, rate = 1, radius = 10.0, maxUpdateInterval = 10000)
    }
    var clientListener: ((Location) -> Unit)? = null
    var listener: LocationListener = object: LocationListener {
        override fun onLocationChanged(location: Location) {
            val loc = locationFilter.filter(location)
            if (loc != null) clientListener?.invoke(loc)
        }

    }
    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        clientListener = null
        provider = LocationServices.getFusedLocationProviderClient(this)
        val req = LocationRequest.Builder(LOCATION_UPDATE_PERIOD)
            .setMaxUpdateAgeMillis(0)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        provider!!.requestLocationUpdates(req, listener, null)

        val pkg = intent?.getStringExtra(PACKAGE_KEY)
        val classname = intent?.getStringExtra(CLASS_NAME_KEY)
        val componentName = if (pkg != null && classname != null) ComponentName(pkg, classname) else null
        if (componentName != null) {
            createChannel()
            startForeground(RUNNING_NOTIFICATION_ID, createNotification(componentName))
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        provider?.removeLocationUpdates(listener)
        provider = null
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return LocalBinder()
    }
    inner class LocalBinder: Binder() {
        fun setListener(listener: (Location) -> Unit) {this@LocationService.clientListener = listener}
    }


    private fun createChannel() {
        val channel = NotificationChannelCompat.Builder(RUNNING_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
            .setName("Running channel")
            .setDescription("Current activities.")
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }
    private fun createNotification(componentName: ComponentName?): Notification {
        val intent = Intent()
        intent.setComponent(componentName)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(context, RUNNING_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, RUNNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.runner)
            .setContentTitle(context.resources.getString(R.string.running_activity_in_progress))
            .setContentIntent(pendingIntent)
            .setColor(RUN_MARKER_COLORS[0].toArgb())
            .setSound(null)
            .build()
        return notification
    }
}