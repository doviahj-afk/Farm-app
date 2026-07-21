package com.farmmanager.app.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.farmmanager.app.FarmApp
import com.farmmanager.app.MainActivity
import com.farmmanager.app.util.AlarmScheduler
import com.farmmanager.app.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Fires when a scheduled reminder alarm goes off — shows a notification and reschedules repeats. */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminderId", -1L)
        val title = intent.getStringExtra("title") ?: "Farm reminder"
        val notes = intent.getStringExtra("notes") ?: ""
        val repeatType = intent.getStringExtra("repeatType") ?: "NONE"

        NotificationHelper.ensureChannel(context)
        showNotification(context, reminderId, title, notes)

        // Reschedule the next occurrence for repeating reminders, and persist it.
        if (repeatType != "NONE" && reminderId >= 0) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as FarmApp
                    val repository = app.repository
                    // Find the matching reminder from a one-shot snapshot.
                    val list = repository.getAllReminders().first()
                    val reminder = list.find { it.id == reminderId }
                    if (reminder != null) {
                        val next = AlarmScheduler.nextOccurrence(reminder)
                        if (next != null) {
                            val updated = reminder.copy(dateTime = next)
                            repository.updateReminder(updated)
                            AlarmScheduler.schedule(context, updated)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun showNotification(context: Context, reminderId: Long, title: String, notes: String) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, reminderId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(notes.ifBlank { "Farm reminder" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(notes.ifBlank { "Farm reminder" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(reminderId.toInt(), notification)
        }
    }
}
