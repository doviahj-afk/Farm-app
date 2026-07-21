package com.farmmanager.app.ui.reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmmanager.app.data.entity.Reminder
import com.farmmanager.app.data.repository.FarmRepository
import com.farmmanager.app.util.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: FarmRepository,
    private val appContext: Context
) : ViewModel() {

    val reminders = repository.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** repeatType: "NONE", "DAILY", "WEEKLY", or "MONTHLY" — mirrors Google Calendar's repeat option. */
    fun addReminder(title: String, dateTime: Long, notes: String, repeatType: String = "NONE") {
        viewModelScope.launch {
            val id = repository.insertReminder(
                Reminder(title = title, dateTime = dateTime, notes = notes, repeatType = repeatType)
            )
            AlarmScheduler.schedule(appContext, Reminder(id = id, title = title, dateTime = dateTime, notes = notes, repeatType = repeatType))
        }
    }

    fun toggleCompleted(reminder: Reminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isCompleted = !reminder.isCompleted)
            repository.updateReminder(updated)
            if (updated.isCompleted) {
                AlarmScheduler.cancel(appContext, updated)
            } else {
                AlarmScheduler.schedule(appContext, updated)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            AlarmScheduler.cancel(appContext, reminder)
            repository.deleteReminder(reminder)
        }
    }
}
