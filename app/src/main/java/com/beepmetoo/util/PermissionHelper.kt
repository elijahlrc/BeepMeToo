package com.beepmetoo.util

import android.Manifest
import android.os.Build

object PermissionHelper {

    /**
     * Returns the list of runtime permissions that need to be requested,
     * based on the device's API level.
     */
    fun getRuntimePermissions(apiLevel: Int = Build.VERSION.SDK_INT): List<String> {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (apiLevel >= 33) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions
    }
}
