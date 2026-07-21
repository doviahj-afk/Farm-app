package com.farmmanager.app.ui.health

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
import com.farmmanager.app.data.entity.HealthRecord
import com.farmmanager.app.ui.flock.FlockViewModel
import com.farmmanager.app.util.DateUtils

private val healthTypes = listOf("Vaccination", "Medication", "Disease", "Checkup")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(viewModel: HealthViewModel, flockViewModel: FlockViewModel) {
    val records by viewModel.records.collectAsState()
    val flocks by flockViewModel.flocks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Health Records") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add health record")
            }
        }
    ) { padding ->
        if (records.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No health records yet. Tap + to log one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    HealthCard(record, flocks.find { it.id == record.flockId }?.name ?: "Unknown flock") {
                        viewModel.deleteRecord(record)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddHealthDialog(
            flocks = flocks,
            onDismiss = { showDialog = false },
            onConfirm = { flockId, type, desc, cost, notes ->
                viewModel.addRecord(flockId, DateUtils.now(), type, desc, cost, notes)
                showDialog = false
            }
        )
    }
}

@Composable
private fun HealthCard(record: HealthRecord, flockName: String, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${record.type} — $flockName", style = MaterialTheme.typography.titleMedium)
                Text(record.description, style = MaterialTheme.typography.bodyMedium)
                Text("${DateUtils.formatDate(record.date)} • cost ${record.cost}", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHealthDialog(
    flocks: List<com.farmmanager.app.data.entity.Flock>,
    onDismiss: () -> Unit,
    onConfirm: (flockId: Long, type: String, description: String, cost: Double, notes: String) -> Unit
) {
    var selectedFlockId by remember { mutableStateOf(flocks.firstOrNull()?.id ?: 0L) }
    var flockExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(healthTypes.first()) }
    var typeExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Health Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = flockExpanded, onExpandedChange = { flockExpanded = it }) {
                    OutlinedTextField(
                        value = flocks.find { it.id == selectedFlockId }?.name ?: "Select flock",
                        onValueChange = {}, readOnly = true, label = { Text("Flock") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = flockExpanded, onDismissRequest = { flockExpanded = false }) {
                        flocks.forEach { flock ->
                            DropdownMenuItem(text = { Text(flock.name) }, onClick = {
                                selectedFlockId = flock.id
                                flockExpanded = false
                            })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedType, onValueChange = {}, readOnly = true, label = { Text("Type") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        healthTypes.forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = {
                                selectedType = type
                                typeExpanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Cost") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedFlockId, selectedType, description, cost.toDoubleOrNull() ?: 0.0, notes)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
