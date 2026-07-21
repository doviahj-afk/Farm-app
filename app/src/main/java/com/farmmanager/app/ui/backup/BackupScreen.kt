package com.farmmanager.app.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(viewModel: BackupViewModel) {
    val status by viewModel.status.collectAsState()
    val driveAccount by viewModel.driveAccount.collectAsState()

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { viewModel.exportEggsCsv(it) }
    }
    val xlsxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri -> uri?.let { viewModel.exportEggsXlsx(it) } }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { viewModel.exportFullBackupJson(it) }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.restoreFromJson(it) }
    }
    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            viewModel.onSignInResult(account)
        } catch (e: ApiException) {
            viewModel.onSignInError(e.statusCode)
        }
    }

    var showRestoreConfirm by remember { mutableStateOf<() -> Unit>({}) }
    var confirmDialogVisible by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Backup & Export") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (val s = status) {
                is BackupStatus.Working -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is BackupStatus.Success -> Text(s.message, color = MaterialTheme.colorScheme.primary)
                is BackupStatus.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                is BackupStatus.Idle -> {}
            }

            SectionTitle("Export egg records")
            ActionRow(Icons.Default.Description, "Export to CSV", "Opens with Excel, Sheets, Numbers") {
                csvLauncher.launch("egg_records.csv")
            }
            ActionRow(Icons.Default.TableChart, "Export to Excel (.xlsx)", "Native spreadsheet format") {
                xlsxLauncher.launch("egg_records.xlsx")
            }

            SectionTitle("Full data backup")
            ActionRow(Icons.Default.CloudUpload, "Backup all data (JSON)", "Flocks, eggs, feed, health, finances, reminders") {
                backupLauncher.launch("farm_manager_backup.json")
            }
            ActionRow(Icons.Default.Restore, "Restore from backup file", "Replaces all current data") {
                confirmDialogVisible = true
                showRestoreConfirm = { restoreLauncher.launch(arrayOf("application/json")) }
            }

            SectionTitle("Google Drive cloud sync")
            if (driveAccount == null) {
                ActionRow(Icons.Default.CloudUpload, "Sign in with Google", "Enables cloud backup") {
                    signInLauncher.launch(viewModel.signInClient().signInIntent)
                }
                Text(
                    "Requires a one-time Google Cloud Console setup (OAuth client ID). See README for steps.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("Signed in as ${driveAccount?.email}", style = MaterialTheme.typography.bodyMedium)
                ActionRow(Icons.Default.CloudDone, "Backup to Drive now", "Uploads your latest data") {
                    viewModel.backupToDrive()
                }
                ActionRow(Icons.Default.Restore, "Restore from Drive", "Replaces all current data") {
                    confirmDialogVisible = true
                    showRestoreConfirm = { viewModel.restoreFromDrive() }
                }
                TextButton(onClick = { viewModel.signOutDrive() }) { Text("Sign out") }
            }
        }
    }

    if (confirmDialogVisible) {
        AlertDialog(
            onDismissRequest = { confirmDialogVisible = false },
            title = { Text("Replace all data?") },
            text = { Text("Restoring a backup will replace everything currently in the app. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDialogVisible = false
                    showRestoreConfirm()
                }) { Text("Restore") }
            },
            dismissButton = { TextButton(onClick = { confirmDialogVisible = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = title)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onClick) { Text("Go") }
        }
    }
}
