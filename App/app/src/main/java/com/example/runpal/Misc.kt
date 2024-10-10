package com.example.runpal

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.net.toFile
import com.example.runpal.activities.MainActivity
import kotlinx.coroutines.delay
import java.io.File
import java.io.InputStream
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin


interface Destination {
    val argsRoute: String
    val baseRoute: String
    val icon: ImageVector?
    val label: Int?
    val title: Int?
}

/**
 * The list should be already sorted in ascending order.
 */
fun<T> binarySearch(data: List<T>, value: (T) -> Float, target: Float): T? {
    var i = 0
    var j = data.size - 1
    if (j < 0) return null
    if (j == 0) return data[0]

    while (j - i > 1) {
        val k = (i + j) / 2
        val valueK = value(data[k])

        if (valueK > target) j = k
        else if (valueK < target) i = k
        else return data[k]
    }

    val valueI = value(data[i])
    val valueJ = value(data[j])
    if (target - valueI <  valueJ - target) return data[i]
    else return data[j]
}

/**
 * @param speed Running speed in m/s.
 * @param slope Slope as a percentage (postive or negative).
 * @param weight Runner's weight in kg.
 *
 * @return kcal per second.
 */
fun kcalExpenditure(speed: Double, slope: Double, weight: Double): Double {
    var factor = 0.2 + 0.9*slope
    if (factor < 0.1) factor = 0.1
    val VO2 = speed * factor / 1000
    val PE = VO2 * weight * 5
    return PE
}
fun waveFloat(mid: Float, range: Float, period: Long, phase: Long, lowerBound: Float, t: Long): Float {
    val sin = sin((t + phase) * 2 * Math.PI.toFloat() / period)
    return max(lowerBound, mid + sin*range)
}

@Composable
fun timeAsState(start: Long = 0L, updateInterval: Long = 20): State<Long> {
    val state = remember { mutableStateOf(System.currentTimeMillis() - start) }
    LaunchedEffect(key1 = null) {
        while(true) {
            state.value = System.currentTimeMillis() - start
            delay(updateInterval)
        }
    }
    return state
}

@Composable
fun animateDoubleAsStateCustom(target: Double, duration: Long, spec: (Double) -> Double = {it}): State<Double> {
    val state = rememberSaveable {
        mutableStateOf(target)
    }
    LaunchedEffect(key1 = target) {
        val start = state.value
        val startTime = System.currentTimeMillis()
        var time = 0L
        val interval = 20L
        while (time < duration) {
            state.value = spec(time/duration.toDouble()) * (target - start) + start
            delay(interval)
            time = System.currentTimeMillis() - startTime
        }
        state.value = target
    }
    return state
}
@Composable
fun animateDpAsStateCustom(target: Dp, duration: Long, spec: (Double) -> Double = {it}): State<Dp> {
    val state = animateDoubleAsStateCustom(target = target.value.toDouble(), duration = duration, spec = spec)
    return object: State<Dp> {
        override val value: Dp
            get() = state.value.dp
    }
}

fun Context.permanentServerFile(name: String): File {
    val imagesDir = File(filesDir, "server")
    if (!imagesDir.exists()) imagesDir.mkdir()
    return File(imagesDir, name)
}
fun Context.tempServerFile(name: String): File {
    val imagesDir = File(cacheDir, "images")
    if (!imagesDir.exists()) imagesDir.mkdir()
    return File(imagesDir, name)
}

/**
 * If the file is not in the application's permanent files diractory,
 * a copy is placed there, and the corresponding File object is returned.
 */
fun Context.makePermanentFile(profileFile: File): File {
    val permanentFile = this.permanentServerFile(profileFile.name)
    if (!profileFile.equals(permanentFile)) {
        //save the file permanently in the app's files directory
        profileFile.copyTo(permanentFile, true)
    }
    return permanentFile
}

/**
 * Clears all activities on the activity stack and starts MainActivity.
 * (I guess?)
 */
fun Context.restartApp() {
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //??
    startActivity(intent)
}

fun Context.hasLocationPermission(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasNotificationPermission(): Boolean {
    if (Build.VERSION.SDK_INT >= 33) return ContextCompat.checkSelfPermission(
        this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    else return true
}

fun limitText(text: String, max: Int = 300): String {
    if (text.length < max) return text
    return text.subSequence(0, max).toString() + "..."
}


suspend fun<T> tryRepeat(reps: Long = 10, interval: (Long) -> Long = {1000L}, producer: suspend () -> T): T {
    var i = 1L
    while(true) {
        try {
            return producer()
        } catch(e: Exception) { if (i == reps) throw e}
        i++
        delay(interval(i))
    }
}
suspend fun<T> tryRepeatExp(reps: Long = 10, producer: suspend () -> T) = tryRepeat(reps = reps, interval = { 50L*exp(it.toDouble()).toLong() }, producer = producer)


fun Color.lightness(factor: Float): Color {
    var argb = this.toArgb()
    val alpha = android.graphics.Color.alpha(argb)
    val red = android.graphics.Color.red(argb)
    val green = android.graphics.Color.green(argb)
    val blue = android.graphics.Color.blue(argb)

    val hsl = FloatArray(3)
    ColorUtils.RGBToHSL(red, green, blue, hsl)
    hsl[2] = factor
    argb = ColorUtils.HSLToColor(hsl) or (alpha shl 24)
    return Color(argb)
}
fun Color.lighter(factor: Float): Color {
    return lerp(this, Color.White, factor)
}
fun Color.darker(factor: Float): Color {
    return lerp(this, Color.Black, factor)
}

fun String.Capitalize(): String {
    return "${first().uppercase()}${substring(1).lowercase()}"
}