package com.farmmanager.app.ui.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmmanager.app.data.repository.FarmRepository
import com.farmmanager.app.util.BackupJson
import com.farmmanager.app.util.ExportUtils
import com.farmmanager.app.util.GoogleDriveBackupManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

sealed class BackupStatus {
    object Idle : BackupStatus()
    object Working : BackupStatus()
    data class Success(val message: String) : BackupStatus()
    data class Error(val message: String) : BackupStatus()
}

class BackupViewModel(
    private val repository: FarmRepository,
    private val appContext: Context
) : ViewModel() {

    private val _status = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val status: StateFlow<BackupStatus> = _status

    private val _driveAccount = MutableStateFlow<GoogleSignInAccount?>(GoogleDriveBackupManager.lastSignedInAccount(appContext))
    val driveAccount: StateFlow<GoogleSignInAccount?> = _driveAccount

    fun signInClient() = GoogleDriveBackupManager.signInClient(appContext)

    fun onSignInResult(account: GoogleSignInAccount?) {
        _driveAccount.value = account
    }

    fun onSignInError(statusCode: Int) {
        // Code 10 = DEVELOPER_ERROR, almost always an OAuth client that isn't set up yet
        // (wrong/missing SHA-1 fingerprint or package name in Google Cloud Console).
        val message = if (statusCode == 10) {
            "Google sign-in isn't set up yet. You need to register an OAuth client in Google Cloud Console first — see the README's 'Enabling Google Drive cloud sync' section."
        } else {
            "Google sign-in failed (code $statusCode)."
        }
        _status.value = BackupStatus.Error(message)
    }

    fun signOutDrive() {
        signInClient().signOut()
        _driveAccount.value = null
    }

    fun exportEggsCsv(uri: Uri) = runExport {
        val flocks = repository.getAllFlocks().first()
        val records = repository.getAllEggRecords().first()
        val rows = records.map { r ->
            listOf(
                com.farmmanager.app.util.DateUtils.formatDate(r.date),
                flocks.find { it.id == r.flockId }?.name ?: "Unknown",
                if (r.cageNumber > 0) r.cageNumber.toString() else "-",
                r.quantityCollected.toString(),
                r.quantityBroken.toString(),
                r.notes
            )
        }
        appContext.contentResolver.openOutputStream(uri)?.use { out ->
            ExportUtils.writeCsv(out, listOf("Date", "Flock", "Cage", "Collected", "Broken", "Notes"), rows)
        }
        "Eggs exported to CSV."
    }

    fun exportEggsXlsx(uri: Uri) = runExport {
        val flocks = repository.getAllFlocks().first()
        val records = repository.getAllEggRecords().first()
        val rows = records.map { r ->
            listOf(
                com.farmmanager.app.util.DateUtils.formatDate(r.date),
                flocks.find { it.id == r.flockId }?.name ?: "Unknown",
                if (r.cageNumber > 0) r.cageNumber.toString() else "-",
                r.quantityCollected.toString(),
                r.quantityBroken.toString(),
                r.notes
            )
        }
        appContext.contentResolver.openOutputStream(uri)?.use { out ->
            ExportUtils.writeXlsx(out, "Eggs", listOf("Date", "Flock", "Cage", "Collected", "Broken", "Notes"), rows)
        }
        "Eggs exported to Excel."
    }

    fun exportFullBackupJson(uri: Uri) = runExport {
        val snapshot = repository.buildSnapshot()
        val json = BackupJson.toJson(snapshot)
        appContext.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(json.toByteArray())
        }
        "Full backup saved."
    }

    fun restoreFromJson(uri: Uri) = runExport {
        val json = appContext.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalStateException("Could not read file.")
        val snapshot = BackupJson.fromJson(json)
        repository.restoreSnapshot(snapshot)
        "Backup restored. All data replaced."
    }

    fun backupToDrive() = runExport {
        val account = _driveAccount.value ?: throw IllegalStateException("Sign in to Google first.")
        val snapshot = repository.buildSnapshot()
        val json = BackupJson.toJson(snapshot)
        GoogleDriveBackupManager.uploadBackup(appContext, account, json).getOrThrow()
        "Backed up to Google Drive."
    }

    fun restoreFromDrive() = runExport {
        val account = _driveAccount.value ?: throw IllegalStateException("Sign in to Google first.")
        val json = GoogleDriveBackupManager.downloadBackup(appContext, account).getOrThrow()
            ?: throw IllegalStateException("No backup found on Drive yet.")
        val snapshot = BackupJson.fromJson(json)
        repository.restoreSnapshot(snapshot)
        "Restored from Google Drive."
    }

    private fun runExport(block: suspend () -> String) {
        viewModelScope.launch {
            _status.value = BackupStatus.Working
            _status.value = try {
                val message = withContext(Dispatchers.IO) { block() }
                BackupStatus.Success(message)
            } catch (e: Exception) {
                BackupStatus.Error(e.message ?: "Something went wrong.")
            }
        }
    }

    fun clearStatus() {
        _status.value = BackupStatus.Idle
    }
}
