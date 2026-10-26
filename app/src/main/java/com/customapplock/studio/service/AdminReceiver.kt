package com.customapplock.studio.service

import android.app.admin.DeviceAdminReceiver

/** Registered as a device admin only to block uninstall while "Uninstall protection" is on. */
class AdminReceiver : DeviceAdminReceiver()
