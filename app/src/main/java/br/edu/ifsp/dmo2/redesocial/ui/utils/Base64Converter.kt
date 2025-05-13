package br.edu.ifsp.dmo2.redesocial.ui.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import java.io.ByteArrayOutputStream

object Base64Converter {
    private const val MAX_IMAGE_SIZE_KB = 800
    private const val INITIAL_QUALITY = 70
    private const val MIN_QUALITY = 30

    fun bitmapToString(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()

        val resizedBitmap = resizeBitmapIfNeeded(bitmap)

        var quality = INITIAL_QUALITY
        do {
            stream.reset()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            quality -= 5
        } while (stream.size() > MAX_IMAGE_SIZE_KB * 1024 && quality >= MIN_QUALITY)

        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap): Bitmap {
        val maxDimension = 1024

        return if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = Math.min(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height
            )
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
    }

    fun stringToBitmap(encodedString: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    fun drawableToString(drawable: Drawable?): String? {
        if (drawable == null) return null

        val bitmap = when {
            drawable is BitmapDrawable -> drawable.bitmap
            else -> {
                val width = drawable.intrinsicWidth.coerceAtLeast(1)
                val height = drawable.intrinsicHeight.coerceAtLeast(1)
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
                    val canvas = Canvas(it)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                }
            }
        }
        return bitmapToString(bitmap)
    }

    fun getDefaultBitmap(): Bitmap {
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawColor(Color.LTGRAY)
        }
    }
}