package com.farmmanager.app.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.farmmanager.app.data.repository.FarmRepository

/**
 * Reflectively builds ViewModels. Most take just a FarmRepository; a few (like ReminderViewModel,
 * which needs to schedule system alarms) also take an application Context.
 */
class ViewModelFactory(
    private val repository: FarmRepository,
    private val appContext: Context? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (appContext != null) {
            try {
                modelClass.getConstructor(FarmRepository::class.java, Context::class.java)
                    .newInstance(repository, appContext)
            } catch (e: NoSuchMethodException) {
                modelClass.getConstructor(FarmRepository::class.java).newInstance(repository)
            }
        } else {
            modelClass.getConstructor(FarmRepository::class.java).newInstance(repository)
        }
    }
}
