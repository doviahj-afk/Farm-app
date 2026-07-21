package com.farmmanager.app.ui.reminder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.farmmanager.app.data.entity.Reminder
import com.farmmanager.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(viewModel: ReminderViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reminders") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add reminder")
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No reminders yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onToggle = { viewModel.toggleCompleted(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddReminderDialog(
            onDismiss = { showDialog = false },
            onConfirm = { title, dateTime, notes, repeatType ->
                viewModel.addReminder(title, dateTime, notes, repeatType)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ReminderCard(reminder: Reminder, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = reminder.isCompleted, onCheckedChange = { onToggle() })
                Column {
                    Text(
                        reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    val repeatSuffix = when (reminder.repeatType) {
                        "DAILY" -> " • repeats daily"
                        "WEEKLY" -> " • repeats weekly"
                        "MONTHLY" -> " • repeats monthly"
                        else -> ""
                    }
                    Text(DateUtils.formatDateTime(reminder.dateTime) + repeatSuffix, style = MaterialTheme.typography.labelSmall)
                    if (reminder.notes.isNotBlank()) {
                        Text(reminder.notes, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}

private val repeatOptions = listOf("NONE" to "Does not repeat", "DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, dateTime: Long, notes: String, repeatType: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var repeatType by remember { mutableStateOf("NONE") }
    var repeatExpanded by remember { mutableStateOf(false) }

    // Default to 1 hour from now so the alarm is always in the future.
    var selectedDateTime by remember { mutableStateOf(DateUtils.now() + 60 * 60 * 1000) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })

                OutlinedTextField(
                    value = DateUtils.formatDateTime(selectedDateTime),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Alarm date & time") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Change date/time")
                        }
                    }
                )

                ExposedDropdownMenuBox(expanded = repeatExpanded, onExpandedChange = { repeatExpanded = it }) {
                    OutlinedTextField(
                        value = repeatOptions.find { it.first == repeatType }?.second ?: "Does not repeat",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat") },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = repeatExpanded, onDismissRequest = { repeatExpanded = false }) {
                        repeatOptions.forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                repeatType = value
                                repeatExpanded = false
                            })
                        }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, selectedDateTime, notes, repeatType) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateTime)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val pickedDate = datePickerState.selectedDateMillis
                    if (pickedDate != null) {
                        // Preserve the time-of-day already chosen, apply the new date.
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = selectedDateTime }
                        val newCal = java.util.Calendar.getInstance().apply {
                            timeInMillis = pickedDate
                            set(java.util.Calendar.HOUR_OF_DAY, cal.get(java.util.Calendar.HOUR_OF_DAY))
                            set(java.util.Calendar.MINUTE, cal.get(java.util.Calendar.MINUTE))
                        }
                        selectedDateTime = newCal.timeInMillis
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = selectedDateTime }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(java.util.Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val newCal = java.util.Calendar.getInstance().apply {
                        timeInMillis = selectedDateTime
                        set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(java.util.Calendar.MINUTE, timePickerState.minute)
                    }
                    selectedDateTime = newCal.timeInMillis
                    showTimePicker = false
                }) { Text("Done") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }
}
