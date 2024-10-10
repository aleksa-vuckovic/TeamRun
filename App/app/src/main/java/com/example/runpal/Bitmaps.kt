package com.example.runpal

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color.alpha
import android.graphics.Color.argb
import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.get
import androidx.core.graphics.set
import com.example.runpal.ui.theme.DarkBlue
import com.example.runpal.ui.theme.DarkPink
import com.example.runpal.ui.theme.DarkPurple
import com.example.runpal.ui.theme.DarkYellow
import com.example.runpal.ui.theme.White
import com.example.runpal.ui.theme.YellowGreen
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private fun bitDistance(x: Int, y: Int, a: Int, b: Int): Int {
    return Math.sqrt(((x-a)*(x-a)+(y-b)*(y-b)).toDouble()).toInt()
}

/**
 * Applies a blur in a circular area around the center of the bitmap, starting from the specified radius.
 *
 * @param blurLevel Defines the amount of blur, at least 1.
 * @param radius Defines the distance from the bitmap center starting from which the blur is applied.
 * @param transition Defines the width of the blur ring.
 * @param targetColor Optionally defines the ARGB color which the blur should gradually fade into as the perimeter is approached.
 *
 * @return Returns a new bitmap with the specified blur applied.
 */
fun Bitmap.outerBlur(blurLevel: Int, radius: Int, transition: Int, targetColor: Color? = null): Bitmap {
    val centerX = this.width/2
    val centerY = this.height/2


    val target = targetColor?.toArgb() ?: 0
    val targetA = alpha(target)
    val targetR = red(target)
    val targetG = green(target)
    val targetB = blue(target)

    check(this.config == Bitmap.Config.ARGB_8888, {"Invalid bitmap config."})
    check(blurLevel > 0, {"Radius must be positive."})
    val result = this.copy(Bitmap.Config.ARGB_8888, true)
    for (i in 0 .. this.width - 1) {
        for (j in 0 .. this.height - 1) {
            val rad = bitDistance(i,j, centerX, centerY)
            val end = (rad - radius).toDouble()/transition
            if (end > 1.0 || end < 0.0) continue

            var a = 0.0
            var r = 0.0
            var g = 0.0
            var b = 0.0

            val cols = mutableListOf<Int>()
            for (x in -blurLevel .. blurLevel) {
                if (i+x >= 0 && i+x < this.width) {
                    //horizontal ray
                    cols.add(this[i+x,j])
                    //regular diagonal ray
                    if (j+x >= 0 && j+x<this.height) cols.add(this[i+x,j+x])
                    //opposite diagonal ray
                    if (j-x >= 0 && j-x<this.height) cols.add(this[i+x,j-x])
                }
                //vertical ray
                if (j+x >= 0 && j+x<this.width) cols.add(this[i,j+x])
            }

            for (col in cols) {a += alpha(col); r+=red(col); g+=green(col); b+=blue(col);}
            a/=cols.size
            r/=cols.size
            g/=cols.size
            b/=cols.size

            a = a*(1-end)+targetA*end
            r = r*(1-end)+targetR*end
            g = g*(1-end)+targetG*end
            b = b*(1-end)+targetB*end
            result.set(i, j, argb(a.toInt(),r.toInt(),g.toInt(),b.toInt()))
        }
    }
    return result
}

/**
 * Adds an empty transparent margin to the bitmap.
 *
 * @param margin The size of the margin.
 *
 * @return A new bitmap, with the original in the center, and a margin of specified size around.
 */
fun Bitmap.addMargin(margin: Int): Bitmap {
    val result = Bitmap.createBitmap(width + margin*2, height + margin*2, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    canvas.drawBitmap(this, margin.toFloat(), margin.toFloat(), null)
    return result
}

fun Bitmap.resize(width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, width, height, true)
}

fun Bitmap.cropCircle(): Bitmap {
    val width = width
    val height = height
    val centerX = width/2
    val centerY = height/2
    val diameter = minOf(width, height)
    val radius = diameter/2
    val result = Bitmap.createBitmap(diameter, diameter, this.config ?: Bitmap.Config.ARGB_8888)
    for (i in 0..width-1)
        for (j in 0..height-1) {
            val r = bitDistance(i, j, centerX, centerY)
            if (r <= radius) {
                try {
                    result.set(i - centerX + radius, j - centerY + radius, this.get(i, j))
                } catch(_: Exception) {}
            }
        }
    return result
}

fun Bitmap.getMarkerBitmap(size: Int, color: Color): Bitmap {
    val iconSize = (size*0.9).toInt()
    val margin = (size*0.05).toInt()
    val blurRadius = (size*0.35).toInt()
    val blurWidth = (size*0.15).toInt()
    return this.cropCircle()
        .resize(iconSize, iconSize)
        .addMargin(margin)
        .outerBlur(20, blurRadius, blurWidth, color.copy(alpha = 0.3f))
}

fun Bitmap.toRequestBody(): RequestBody {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray().toRequestBody("image/png".toMediaType())
}
fun Bitmap.toMultipartPart(fieldName: String = "image", fileName: String = "image.png"): MultipartBody.Part {
    return MultipartBody.Part.createFormData(fieldName, fileName, this.toRequestBody())
}

/**
 * Reads the image pointed to by the URI, and returns it as a bitmap.
 *
 * @return The bitamp of the image pointed to by the URI,
 * or null in case of an error.
 */
fun Uri.getBitmap(contentResolver: ContentResolver): Bitmap? {
    var inputStream: InputStream? = null
    try {
        inputStream = contentResolver.openInputStream(this)
        if (inputStream != null) {
            return BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        inputStream?.close()
    }
    return null
}

/**
 * Saves the bitmap image as a temporary PNG file in the cache directory
 * and returns the corresponding URI.
 * The name parameter should be a unique name for the image, with the appropriate
 * .png extension. If the file already exists, it will be overwritten.
 *
 * @return URI of the saved image, or null in case of error.
 */
fun Bitmap.cacheUri(context: Context, name: String): Uri? {
    var uri: Uri? = null
    var fileOutputStream: FileOutputStream? = null
    try {
        val file = File(context.cacheDir, name)
        fileOutputStream = FileOutputStream(file)
        this.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        uri = Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            fileOutputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return uri
}

object BitmapDescriptorStore {
    private val simpleMarkers = mutableMapOf<Pair<Color, Int>, BitmapDescriptor>()
    private val checkerMarkers = mutableMapOf<Pair<Int,Int>, BitmapDescriptor>()
    private fun generateCircleBitmap(size: Int, color: Color): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val r = size/2
        val ri = r/8
        for (i in 0..size-1) {
            for (j in 0..size-1) {
                val d = bitDistance(i, j, r, r)
                if (d < r-ri) bitmap.set(i, j, color.toArgb())
                else if (d < r) bitmap.set(i, j, Color.White.toArgb())
            }
        }
        return bitmap
    }

    private fun generateCheckerFlagBitmap(width: Int, height: Int, pole: Int): Bitmap {
        val tile = width/5;
        val bitmap = Bitmap.createBitmap(width, height + pole, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val blackPaint = Paint().apply { color = Color.Black.toArgb() }
        val whitePaint = Paint().apply { color = Color.White.toArgb() }

        //draw flag
        for (x in 0 until width step tile) {
            for (y in 0 until height step tile) {
                val paint = if ((x/tile + y/tile) % 2 == 0) blackPaint else whitePaint
                canvas.drawRect(
                    x.toFloat(),
                    y.toFloat(),
                    (if (x + tile > width) width else x + tile).toFloat(),
                    (if (y + tile > height) height else y + tile).toFloat(),
                    paint
                )
            }
        }
        //draw pole
        canvas.drawRect(0f, height.toFloat(), width/10f, height.toFloat()+pole,blackPaint)
        return bitmap
    }

    fun getCircle(color: Color, size: Int = 35): BitmapDescriptor {
        if (!simpleMarkers.containsKey(color to size))
            simpleMarkers[color to size] = BitmapDescriptorFactory.fromBitmap(generateCircleBitmap(size, color))
        return simpleMarkers[color to size]!!
    }

    fun getCheckerFlag(width: Int, height: Int): BitmapDescriptor {
        if (!checkerMarkers.containsKey(width to height))
            checkerMarkers[width to height] = BitmapDescriptorFactory.fromBitmap(
                generateCheckerFlagBitmap(width, height, (1.2f*width).toInt())
            )
        return checkerMarkers[width to height]!!
    }
}