package com.customapplock.studio.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.customapplock.studio.service.AdminReceiver

object DeviceAdmin {
    private fun component(ctx: Context) = ComponentName(ctx, AdminReceiver::class.java)

    private fun dpm(ctx: Context) = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    fun isActive(ctx: Context): Boolean = runCatching { dpm(ctx).isAdminActive(component(ctx)) }.getOrDefault(false)

    fun activateIntent(ctx: Context): Intent =
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component(ctx))
            .putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Stops Lock from being uninstalled by someone else. You can turn it off from inside the app."
            )

    fun deactivate(ctx: Context) {
        runCatching { dpm(ctx).removeActiveAdmin(component(ctx)) }
    }
}
