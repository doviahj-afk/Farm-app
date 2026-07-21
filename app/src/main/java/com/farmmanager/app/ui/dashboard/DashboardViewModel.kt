package com.farmmanager.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmmanager.app.data.entity.TransactionType
import com.farmmanager.app.data.repository.FarmRepository
import com.farmmanager.app.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val totalBirds: Int = 0,
    val eggsThisMonth: Int = 0,
    val incomeThisMonth: Double = 0.0,
    val expenseThisMonth: Double = 0.0,
    val feedCostThisMonth: Double = 0.0
)

class DashboardViewModel(repository: FarmRepository) : ViewModel() {

    private val start = DateUtils.startOfMonth()
    private val end = DateUtils.endOfMonth()

    val uiState = combine(
        repository.getTotalBirds(),
        repository.getTotalEggsCollectedBetween(start, end),
        repository.getTotalByTypeBetween(TransactionType.INCOME, start, end),
        repository.getTotalByTypeBetween(TransactionType.EXPENSE, start, end),
        repository.getTotalFeedCostBetween(start, end)
    ) { birds, eggs, income, expense, feedCost ->
        DashboardUiState(birds, eggs, income, expense, feedCost)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
