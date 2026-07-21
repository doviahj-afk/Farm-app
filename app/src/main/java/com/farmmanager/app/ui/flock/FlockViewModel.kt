package com.farmmanager.app.ui.flock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmmanager.app.data.entity.Flock
import com.farmmanager.app.data.repository.FarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlockViewModel(private val repository: FarmRepository) : ViewModel() {

    val flocks = repository.getAllFlocks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFlock(
        name: String,
        breed: String,
        quantity: Int,
        date: Long,
        cost: Double,
        notes: String,
        hatchDate: Long? = null,
        cageCount: Int = 1
    ) {
        viewModelScope.launch {
            repository.insertFlock(
                Flock(
                    name = name,
                    breed = breed,
                    quantity = quantity,
                    acquisitionDate = date,
                    acquisitionCost = cost,
                    notes = notes,
                    hatchDate = hatchDate,
                    cageCount = cageCount.coerceIn(1, 80)
                )
            )
        }
    }

    fun updateFlock(flock: Flock) {
        viewModelScope.launch { repository.updateFlock(flock) }
    }

    fun deleteFlock(flock: Flock) {
        viewModelScope.launch { repository.deleteFlock(flock) }
    }
}
