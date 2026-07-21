package com.farmmanager.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {
    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val displayFormatTime = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val monthLabelFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    private val yearKeyFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    fun formatDate(millis: Long): String = displayFormat.format(millis)
    fun formatDateTime(millis: Long): String = displayFormatTime.format(millis)

    fun now(): Long = System.currentTimeMillis()

    /** Groups records by calendar day, "yyyy-MM-dd" key, most recent first. */
    fun dayKey(millis: Long): String = dayKeyFormat.format(millis)

    /** Groups records by calendar month, "yyyy-MM" key. */
    fun monthKey(millis: Long): String = monthKeyFormat.format(millis)
    fun monthLabel(millis: Long): String = monthLabelFormat.format(millis)

    /** Groups records by calendar year, "yyyy" key. */
    fun yearKey(millis: Long): String = yearKeyFormat.format(millis)

    /**
     * Human-readable age like "1 yr 3 mo" or "5 mo" or "12 days", given a hatch/birth date.
     * Returns null if hatchDate is null.
     */
    fun ageLabel(hatchDateMillis: Long?): String? {
        if (hatchDateMillis == null) return null
        val hatch = Calendar.getInstance().apply { timeInMillis = hatchDateMillis }
        val today = Calendar.getInstance()
        var years = today.get(Calendar.YEAR) - hatch.get(Calendar.YEAR)
        var months = today.get(Calendar.MONTH) - hatch.get(Calendar.MONTH)
        var days = today.get(Calendar.DAY_OF_MONTH) - hatch.get(Calendar.DAY_OF_MONTH)
        if (days < 0) {
            months -= 1
            val prevMonth = Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.MONTH, -1)
            }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years -= 1
            months += 12
        }
        return when {
            years > 0 && months > 0 -> "$years yr $months mo"
            years > 0 -> "$years yr"
            months > 0 && days > 0 -> "$months mo $days d"
            months > 0 -> "$months mo"
            else -> "$days d"
        }
    }

    fun startOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        return cal.timeInMillis
    }
}
