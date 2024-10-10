package com.example.runpal

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.example.runpal.ui.theme.DarkBlue
import com.example.runpal.ui.theme.DarkPink
import com.example.runpal.ui.theme.DarkPurple
import com.example.runpal.ui.theme.DarkYellow
import com.example.runpal.ui.theme.YellowGreen
import com.google.android.gms.maps.model.BitmapDescriptorFactory

const val MAX_IMAGE_SIZE = 5*1024*1024
const val LOCATION_UPDATE_PERIOD: Long = 100L
val DEFAULT_PROFILE_URI = Uri.parse("android.resource://com.example.runpal/" + R.drawable.default_prof)
const val KM_TO_MILE: Double = 0.621371
const val MILE_TO_FT: Double = 5280.0
const val LB_TO_KG: Double = 0.45359237
const val KM_TO_M: Double = 1000.0
const val EARTH_RADIUS_M: Double =  6371000.0
const val RUN_MARKER_SIZE: Int = 100
const val DEFAULT_ZOOM: Float = 15f
const val RUN_ID_KEY: String = "RUN_ID"
const val ROOM_ID_KEY: String = "ROOM_ID"
const val EVENT_ID_KEY: String = "EVENT_ID"
const val MAP_ID: String = "b0a63e284945e8db"
const val WORK_NAME: String = "WORK"
//const val SERVER_ADDRESS = "https://run-pal-server-f256155400de.herokuapp.com/"
const val SERVER_ADDRESS = "https://16.171.197.131:4000/"
//const val SERVER_ADDRESS = "http://192.168.100.8:4000/"
val LIVE_RANKING_ADDRESS: String
    get() = SERVER_ADDRESS + "event/rankingsubscribe/"
//const val SERVER_ADDRESS = "http://192.168.100.8:4000/"
//const val SERVER_ADDRESS = "http://10.174.9.184:4000/"
//const val SERVER_ADDRESS = "http://192.168.0.7:4000/"
//const val SERVER_ADDRESS = "http://192.168.43.89:4000/"


const val ACTION_DAILY_REMINDER = "com.example.runpal.ACTION_DAILY_REMINDER"
const val ACTION_CURRENT_REMINDER = "com.example.runpal.ACTION_CURRENT_REMINDER"
const val REMINDER_REQUEST_CODE = 1
const val REMINDER_CHANNEL_ID = "REMINDER_CHANNEL"
const val EVENT_DEEP_LINK_URI = "https://runpal.example.com/event/"
const val EVENT_VIEW_REQUEST_CODE = 1
const val REMINDER_NOTIFICATION_ID = 1

const val RUNNING_CHANNEL_ID = "RUNNING_ACTIVITY"
const val RUNNING_NOTIFICATION_ID = 1
const val RUNNING_REQUEST_CODE = 1

const val CLASS_NAME_KEY = "CLASSNAME"
const val PACKAGE_KEY = "PACKAGE"

val RUN_MARKER_COLORS: List<Color> = listOf(DarkBlue, DarkPurple, DarkPink, DarkYellow, YellowGreen)
val MAP_BUTTON_ALPHA = 0.6f
val MAP_BUTTON_COLOR = Color.White.copy(alpha = MAP_BUTTON_ALPHA)