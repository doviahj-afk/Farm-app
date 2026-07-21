package com.farmmanager.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_ID = "farm_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Farm Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alarms for farm reminders — feeding, vaccination, egg collection, etc."
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}
