package com.farmmanager.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.farmmanager.app.data.entity.Reminder
import com.farmmanager.app.receiver.ReminderAlarmReceiver

/**
 * Schedules and cancels real device alarms for Reminder entities, similar to how
 * Google Calendar fires event notifications at a specific date/time.
 */
object AlarmScheduler {

    private fun pendingIntent(context: Context, reminder: Reminder): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = "com.farmmanager.app.ACTION_REMINDER_ALARM"
            putExtra("reminderId", reminder.id)
            putExtra("title", reminder.title)
            putExtra("notes", reminder.notes)
            putExtra("repeatType", reminder.repeatType)
            putExtra("dateTime", reminder.dateTime)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(context: Context, reminder: Reminder) {
        if (reminder.isCompleted) return
        if (reminder.dateTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, reminder)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // Falls back to an inexact alarm if the user hasn't granted exact-alarm permission.
                alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.dateTime, pi)
                return
            }
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.dateTime, pi)
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.dateTime, pi)
        }
    }

    fun cancel(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, reminder))
    }

    /** Computes the next occurrence for a repeating reminder after it has fired. */
    fun nextOccurrence(reminder: Reminder): Long? {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = reminder.dateTime }
        return when (reminder.repeatType) {
            "DAILY" -> { cal.add(java.util.Calendar.DAY_OF_YEAR, 1); cal.timeInMillis }
            "WEEKLY" -> { cal.add(java.util.Calendar.WEEK_OF_YEAR, 1); cal.timeInMillis }
            "MONTHLY" -> { cal.add(java.util.Calendar.MONTH, 1); cal.timeInMillis }
            else -> null
        }
    }
}
