package com.beepmetoo.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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
}
