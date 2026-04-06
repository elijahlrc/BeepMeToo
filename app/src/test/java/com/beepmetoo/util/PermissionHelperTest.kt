package com.beepmetoo.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PermissionHelperTest {

    @Test
    fun `requiredPermissions includes POST_NOTIFICATIONS on API 33+`() {
        val perms = PermissionHelper.getRuntimePermissions(apiLevel = 33)
        assertThat(perms).contains(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun `requiredPermissions excludes POST_NOTIFICATIONS below API 33`() {
        val perms = PermissionHelper.getRuntimePermissions(apiLevel = 32)
        assertThat(perms).doesNotContain(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun `requiredPermissions always includes CAMERA`() {
        val perms26 = PermissionHelper.getRuntimePermissions(apiLevel = 26)
        val perms34 = PermissionHelper.getRuntimePermissions(apiLevel = 34)
        assertThat(perms26).contains(android.Manifest.permission.CAMERA)
        assertThat(perms34).contains(android.Manifest.permission.CAMERA)
    }
}
