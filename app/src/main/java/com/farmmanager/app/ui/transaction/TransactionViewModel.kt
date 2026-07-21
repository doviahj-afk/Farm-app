package com.farmmanager.app.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmmanager.app.data.entity.TransactionEntity
import com.farmmanager.app.data.entity.TransactionType
import com.farmmanager.app.data.repository.FarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: FarmRepository) : ViewModel() {

    val transactions = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(type: TransactionType, category: String, date: Long, amount: Double, description: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(type = type, category = category, date = date, amount = amount, description = description)
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }
}
