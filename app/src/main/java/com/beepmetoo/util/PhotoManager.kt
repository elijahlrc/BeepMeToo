package com.beepmetoo.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val photosDir: File
        get() = File(context.filesDir, "photos").also { it.mkdirs() }

    fun getPhotoFile(sampleId: Long): File =
        File(photosDir, "sample_${sampleId}.jpg")

    fun createTempFile(): File =
        File.createTempFile("beep_photo_", ".jpg", photosDir)

    /**
     * Scale an image file down to [maxDimension] on its longest side, overwriting in place.
     * Returns the file path.
     */
    fun scaleImage(file: File, maxDimension: Int = 1024): String {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)

        val width = options.outWidth
        val height = options.outHeight
        if (width <= maxDimension && height <= maxDimension) return file.absolutePath

        val scaleFactor = maxOf(width, height).toFloat() / maxDimension
        val scaledWidth = (width / scaleFactor).toInt()
        val scaledHeight = (height / scaleFactor).toInt()

        val fullBitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return file.absolutePath
        val scaled = Bitmap.createScaledBitmap(fullBitmap, scaledWidth, scaledHeight, true)
        fullBitmap.recycle()

        FileOutputStream(file).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        scaled.recycle()

        return file.absolutePath
    }
}
