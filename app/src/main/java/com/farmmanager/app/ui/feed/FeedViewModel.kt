package com.farmmanager.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmmanager.app.data.entity.FeedRecord
import com.farmmanager.app.data.repository.FarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedViewModel(private val repository: FarmRepository) : ViewModel() {

    val records = repository.getAllFeedRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecord(flockId: Long, feedName: String, date: Long, quantityKg: Double, cost: Double, notes: String) {
        viewModelScope.launch {
            repository.insertFeedRecord(
                FeedRecord(flockId = flockId, feedName = feedName, date = date, quantityKg = quantityKg, cost = cost, notes = notes)
            )
        }
    }

    fun deleteRecord(record: FeedRecord) {
        viewModelScope.launch { repository.deleteFeedRecord(record) }
    }
}
