package com.farmmanager.app.ui.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farmmanager.app.data.entity.FeedRecord
import com.farmmanager.app.ui.flock.FlockViewModel
import com.farmmanager.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: FeedViewModel, flockViewModel: FlockViewModel) {
    val records by viewModel.records.collectAsState()
    val flocks by flockViewModel.flocks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Feed Management") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add feed record")
            }
        }
    ) { padding ->
        if (records.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No feed records yet. Tap + to log feeding.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    FeedCard(record, flocks.find { it.id == record.flockId }?.name ?: "Unknown flock") {
                        viewModel.deleteRecord(record)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddFeedDialog(
            flocks = flocks,
            onDismiss = { showDialog = false },
            onConfirm = { flockId, feedName, qty, cost, notes ->
                viewModel.addRecord(flockId, feedName, DateUtils.now(), qty, cost, notes)
                showDialog = false
            }
        )
    }
}

@Composable
private fun FeedCard(record: FeedRecord, flockName: String, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${record.feedName} — $flockName", style = MaterialTheme.typography.titleMedium)
                Text("${record.quantityKg} kg • cost ${record.cost}", style = MaterialTheme.typography.bodyMedium)
                Text(DateUtils.formatDate(record.date), style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFeedDialog(
    flocks: List<com.farmmanager.app.data.entity.Flock>,
    onDismiss: () -> Unit,
    onConfirm: (flockId: Long, feedName: String, qty: Double, cost: Double, notes: String) -> Unit
) {
    var selectedFlockId by remember { mutableStateOf(flocks.firstOrNull()?.id ?: 0L) }
    var expanded by remember { mutableStateOf(false) }
    var feedName by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Feeding") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = flocks.find { it.id == selectedFlockId }?.name ?: "Select flock",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Flock") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        flocks.forEach { flock ->
                            DropdownMenuItem(text = { Text(flock.name) }, onClick = {
                                selectedFlockId = flock.id
                                expanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(value = feedName, onValueChange = { feedName = it }, label = { Text("Feed name") })
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity (kg)") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedFlockId, feedName, qty.toDoubleOrNull() ?: 0.0, cost.toDoubleOrNull() ?: 0.0, notes)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
