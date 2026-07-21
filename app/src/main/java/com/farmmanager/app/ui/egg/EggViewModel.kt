package com.farmmanager.app.ui.egg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmmanager.app.data.entity.EggRecord
import com.farmmanager.app.data.repository.FarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EggViewModel(private val repository: FarmRepository) : ViewModel() {

    val records = repository.getAllEggRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecord(flockId: Long, date: Long, collected: Int, broken: Int, notes: String, cageNumber: Int = 0) {
        viewModelScope.launch {
            repository.insertEggRecord(
                EggRecord(
                    flockId = flockId,
                    date = date,
                    quantityCollected = collected,
                    quantityBroken = broken,
                    notes = notes,
                    cageNumber = cageNumber.coerceIn(0, 80)
                )
            )
        }
    }

    fun deleteRecord(record: EggRecord) {
        viewModelScope.launch { repository.deleteEggRecord(record) }
    }
}
