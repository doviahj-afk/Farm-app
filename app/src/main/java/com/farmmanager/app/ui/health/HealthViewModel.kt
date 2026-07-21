package com.farmmanager.app.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmmanager.app.data.entity.HealthRecord
import com.farmmanager.app.data.repository.FarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(private val repository: FarmRepository) : ViewModel() {

    val records = repository.getAllHealthRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecord(flockId: Long, date: Long, type: String, description: String, cost: Double, notes: String) {
        viewModelScope.launch {
            repository.insertHealthRecord(
                HealthRecord(flockId = flockId, date = date, type = type, description = description, cost = cost, notes = notes)
            )
        }
    }

    fun deleteRecord(record: HealthRecord) {
        viewModelScope.launch { repository.deleteHealthRecord(record) }
    }
}
