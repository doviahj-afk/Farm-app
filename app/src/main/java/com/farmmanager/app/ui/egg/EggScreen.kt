package com.farmmanager.app.ui.egg

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
import com.farmmanager.app.data.entity.EggRecord
import com.farmmanager.app.ui.flock.FlockViewModel
import com.farmmanager.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EggScreen(viewModel: EggViewModel, flockViewModel: FlockViewModel) {
    val records by viewModel.records.collectAsState()
    val flocks by flockViewModel.flocks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Egg Production") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add record")
            }
        }
    ) { padding ->
        if (records.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No egg records yet. Tap + to log collection.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    EggCard(record, flocks.find { it.id == record.flockId }?.name ?: "Unknown flock") {
                        viewModel.deleteRecord(record)
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEggDialog(
            flocks = flocks,
            onDismiss = { showDialog = false },
            onConfirm = { flockId, collected, broken, notes, cage ->
                viewModel.addRecord(flockId, DateUtils.now(), collected, broken, notes, cage)
                showDialog = false
            }
        )
    }
}

@Composable
private fun EggCard(record: EggRecord, flockName: String, onDelete: () -> Unit) {
    Card(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    if (record.cageNumber > 0) "$flockName • Cage ${record.cageNumber}" else flockName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text("${record.quantityCollected} collected, ${record.quantityBroken} broken", style = MaterialTheme.typography.bodyMedium)
                Text(DateUtils.formatDate(record.date), style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEggDialog(
    flocks: List<com.farmmanager.app.data.entity.Flock>,
    onDismiss: () -> Unit,
    onConfirm: (flockId: Long, collected: Int, broken: Int, notes: String, cageNumber: Int) -> Unit
) {
    var selectedFlockId by remember { mutableStateOf(flocks.firstOrNull()?.id ?: 0L) }
    var expanded by remember { mutableStateOf(false) }
    var cageExpanded by remember { mutableStateOf(false) }
    var selectedCage by remember { mutableStateOf(0) } // 0 = whole flock
    var collected by remember { mutableStateOf("") }
    var broken by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }

    val selectedFlock = flocks.find { it.id == selectedFlockId }
    val maxCages = (selectedFlock?.cageCount ?: 1).coerceIn(1, 80)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Egg Collection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedFlock?.name ?: "Select flock",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Flock") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        flocks.forEach { flock ->
                            DropdownMenuItem(text = { Text(flock.name) }, onClick = {
                                selectedFlockId = flock.id
                                selectedCage = 0
                                expanded = false
                            })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = cageExpanded, onExpandedChange = { cageExpanded = it }) {
                    OutlinedTextField(
                        value = if (selectedCage == 0) "Whole flock (no specific cage)" else "Cage $selectedCage",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cage (1-$maxCages)") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = cageExpanded, onDismissRequest = { cageExpanded = false }) {
                        DropdownMenuItem(text = { Text("Whole flock (no specific cage)") }, onClick = {
                            selectedCage = 0
                            cageExpanded = false
                        })
                        (1..maxCages).forEach { cage ->
                            DropdownMenuItem(text = { Text("Cage $cage") }, onClick = {
                                selectedCage = cage
                                cageExpanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(value = collected, onValueChange = { collected = it }, label = { Text("Eggs collected") })
                OutlinedTextField(value = broken, onValueChange = { broken = it }, label = { Text("Eggs broken") })
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedFlockId, collected.toIntOrNull() ?: 0, broken.toIntOrNull() ?: 0, notes, selectedCage)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
