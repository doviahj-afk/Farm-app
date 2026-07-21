package com.farmmanager.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Farm Manager") }) }) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { StatCard("Total Birds", state.totalBirds.toString()) }
            item { StatCard("Eggs This Month", state.eggsThisMonth.toString()) }
            item { StatCard("Income (Month)", "%.2f".format(state.incomeThisMonth)) }
            item { StatCard("Expenses (Month)", "%.2f".format(state.expenseThisMonth)) }
            item { StatCard("Feed Cost (Month)", "%.2f".format(state.feedCostThisMonth)) }
            item {
                StatCard(
                    "Net (Month)",
                    "%.2f".format(state.incomeThisMonth - state.expenseThisMonth - state.feedCostThisMonth)
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
