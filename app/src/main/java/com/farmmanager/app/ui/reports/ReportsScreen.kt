package com.farmmanager.app.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farmmanager.app.data.entity.EggRecord
import com.farmmanager.app.ui.egg.EggViewModel
import com.farmmanager.app.ui.feed.FeedViewModel
import com.farmmanager.app.ui.flock.FlockViewModel
import com.farmmanager.app.ui.transaction.TransactionViewModel
import com.farmmanager.app.data.entity.TransactionType
import com.farmmanager.app.util.DateUtils

private enum class ReportTab(val label: String) { OVERVIEW("Overview"), DAILY("Daily"), MONTHLY("Monthly"), YEARLY("Yearly"), CAGES("By Cage") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    eggViewModel: EggViewModel,
    feedViewModel: FeedViewModel,
    transactionViewModel: TransactionViewModel,
    flockViewModel: FlockViewModel
) {
    val eggRecords by eggViewModel.records.collectAsState()
    val feedRecords by feedViewModel.records.collectAsState()
    val transactions by transactionViewModel.transactions.collectAsState()
    val flocks by flockViewModel.flocks.collectAsState()

    var tab by remember { mutableStateOf(ReportTab.OVERVIEW) }

    Scaffold(topBar = { TopAppBar(title = { Text("Reports") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = tab.ordinal) {
                ReportTab.values().forEach { t ->
                    Tab(selected = tab == t, onClick = { tab = t }, text = { Text(t.label) })
                }
            }

            when (tab) {
                ReportTab.OVERVIEW -> OverviewTab(eggRecords, feedRecords, transactions)
                ReportTab.DAILY -> BucketedEggTab(eggRecords) { DateUtils.dayKey(it) to DateUtils.formatDate(it) }
                ReportTab.MONTHLY -> BucketedEggTab(eggRecords) { DateUtils.monthKey(it) to DateUtils.monthLabel(it) }
                ReportTab.YEARLY -> BucketedEggTab(eggRecords) { DateUtils.yearKey(it) to DateUtils.yearKey(it) }
                ReportTab.CAGES -> ByCageTab(eggRecords, flocks)
            }
        }
    }
}

@Composable
private fun OverviewTab(
    eggRecords: List<EggRecord>,
    feedRecords: List<com.farmmanager.app.data.entity.FeedRecord>,
    transactions: List<com.farmmanager.app.data.entity.TransactionEntity>
) {
    val totalEggsCollected = eggRecords.sumOf { it.quantityCollected }
    val totalEggsBroken = eggRecords.sumOf { it.quantityBroken }
    val totalFeedCost = feedRecords.sumOf { it.cost }
    val totalFeedKg = feedRecords.sumOf { it.quantityKg }
    val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val netProfit = totalIncome - totalExpense

    val today = DateUtils.dayKey(DateUtils.now())
    val thisMonth = DateUtils.monthKey(DateUtils.now())
    val thisYear = DateUtils.yearKey(DateUtils.now())
    val eggsToday = eggRecords.filter { DateUtils.dayKey(it.date) == today }.sumOf { it.quantityCollected }
    val eggsThisMonth = eggRecords.filter { DateUtils.monthKey(it.date) == thisMonth }.sumOf { it.quantityCollected }
    val eggsThisYear = eggRecords.filter { DateUtils.yearKey(it.date) == thisYear }.sumOf { it.quantityCollected }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReportSectionTitle("Egg Production") }
        item { ReportRow("Today", "$eggsToday eggs") }
        item { ReportRow("This month", "$eggsThisMonth eggs") }
        item { ReportRow("This year", "$eggsThisYear eggs") }
        item { ReportRow("All-time collected", "$totalEggsCollected eggs") }
        item { ReportRow("All-time broken", "$totalEggsBroken eggs") }

        item { ReportSectionTitle("Feed") }
        item { ReportRow("Total feed used", "%.1f kg".format(totalFeedKg)) }
        item { ReportRow("Total feed cost", "%.2f".format(totalFeedCost)) }

        item { ReportSectionTitle("Finances") }
        item { ReportRow("Total income", "%.2f".format(totalIncome)) }
        item { ReportRow("Total expenses", "%.2f".format(totalExpense)) }
        item { ReportRow("Net profit", "%.2f".format(netProfit), highlight = true) }
    }
}

@Composable
private fun BucketedEggTab(eggRecords: List<EggRecord>, bucketOf: (Long) -> Pair<String, String>) {
    val grouped = eggRecords
        .groupBy { bucketOf(it.date) }
        .toList()
        .sortedByDescending { it.first.first }

    if (grouped.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No egg records yet.")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(grouped, key = { it.first.first }) { (bucket, records) ->
            val collected = records.sumOf { it.quantityCollected }
            val broken = records.sumOf { it.quantityBroken }
            ReportRow(bucket.second, "$collected collected, $broken broken")
        }
    }
}

@Composable
private fun ByCageTab(eggRecords: List<EggRecord>, flocks: List<com.farmmanager.app.data.entity.Flock>) {
    val grouped = eggRecords
        .filter { it.cageNumber > 0 }
        .groupBy { it.flockId to it.cageNumber }
        .toList()
        .sortedWith(compareBy({ it.first.first }, { it.first.second }))

    if (grouped.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No per-cage records yet. Select a cage when logging eggs.")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(grouped, key = { "${it.first.first}-${it.first.second}" }) { (key, records) ->
            val (flockId, cage) = key
            val flockName = flocks.find { it.id == flockId }?.name ?: "Unknown flock"
            val collected = records.sumOf { it.quantityCollected }
            val broken = records.sumOf { it.quantityBroken }
            ReportRow("$flockName • Cage $cage", "$collected collected, $broken broken")
        }
    }
}

@Composable
private fun ReportSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun ReportRow(label: String, value: String, highlight: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                value,
                style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
            )
        }
    }
}
