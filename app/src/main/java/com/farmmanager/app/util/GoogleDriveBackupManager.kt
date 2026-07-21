package com.farmmanager.app.util

import android.accounts.Account
import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Backs up/restores the farm data JSON to the signed-in user's Google Drive "app data" folder
 * using plain REST calls to the Drive v3 API (no heavy googleapis client library — keeps the
 * app lightweight, same philosophy as the rest of Joshua's projects).
 *
 * SETUP REQUIRED before this works (one-time, in Google Cloud Console):
 *  1. Create/select a project at https://console.cloud.google.com
 *  2. Enable the "Google Drive API"
 *  3. Create an OAuth 2.0 Client ID of type "Android", using this app's package name
 *     (com.farmmanager.app) and its signing certificate SHA-1 fingerprint
 *     (get it with: ./gradlew signingReport, or `keytool -list -v -keystore <your.keystore>`)
 *  4. No google-services.json is needed for this REST-based approach — the Android OAuth
 *     client ID registered against the SHA-1 + package name is enough.
 */
object GoogleDriveBackupManager {

    private const val BACKUP_FILE_NAME = "farm_manager_backup.json"
    private const val DRIVE_SCOPE = "oauth2:https://www.googleapis.com/auth/drive.appdata"

    fun signInClient(context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun lastSignedInAccount(context: Context): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    private suspend fun getAccessToken(context: Context, account: GoogleSignInAccount): String =
        withContext(Dispatchers.IO) {
            val androidAccount = Account(account.email, "com.google")
            GoogleAuthUtil.getToken(context, androidAccount, DRIVE_SCOPE)
        }

    /** Uploads (or overwrites) the backup JSON in the app-private Drive appDataFolder. */
    suspend fun uploadBackup(context: Context, account: GoogleSignInAccount, json: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAccessToken(context, account)
                val existingId = findBackupFileId(token)

                if (existingId != null) {
                    // File already exists — simple media overwrite is enough, parent stays the same.
                    val url = URL("https://www.googleapis.com/upload/drive/v3/files/$existingId?uploadType=media")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "PATCH"
                        setRequestProperty("Authorization", "Bearer $token")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                    }
                    conn.outputStream.use { it.write(json.toByteArray()) }
                    if (conn.responseCode !in 200..299) {
                        return@withContext Result.failure(Exception("Upload failed: ${conn.responseCode} ${readBody(conn)}"))
                    }
                } else {
                    // New file — use multipart so we can set the name + appDataFolder parent
                    // in the same request as the content (a plain media upload can't set parents).
                    val boundary = "farmmanager_boundary_${System.currentTimeMillis()}"
                    val metadata = org.json.JSONObject().apply {
                        put("name", BACKUP_FILE_NAME)
                        put("parents", org.json.JSONArray(listOf("appDataFolder")))
                    }
                    val body = buildString {
                        append("--$boundary\r\n")
                        append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
                        append(metadata.toString())
                        append("\r\n--$boundary\r\n")
                        append("Content-Type: application/json\r\n\r\n")
                        append(json)
                        append("\r\n--$boundary--")
                    }

                    val url = URL("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Authorization", "Bearer $token")
                        setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
                        doOutput = true
                    }
                    conn.outputStream.use { it.write(body.toByteArray()) }
                    if (conn.responseCode !in 200..299) {
                        return@withContext Result.failure(Exception("Upload failed: ${conn.responseCode} ${readBody(conn)}"))
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Downloads the backup JSON from Drive appDataFolder, or null if none exists yet. */
    suspend fun downloadBackup(context: Context, account: GoogleSignInAccount): Result<String?> =
        withContext(Dispatchers.IO) {
            try {
                val token = getAccessToken(context, account)
                val fileId = findBackupFileId(token) ?: return@withContext Result.success(null)

                val url = URL("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Authorization", "Bearer $token")
                }
                if (conn.responseCode !in 200..299) {
                    return@withContext Result.failure(Exception("Download failed: ${conn.responseCode}"))
                }
                Result.success(readBody(conn))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun findBackupFileId(token: String): String? {
        val query = URLEncoder.encode("name = '$BACKUP_FILE_NAME'", "UTF-8")
        val url = URL("https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=$query&fields=files(id,name)")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Bearer $token")
        }
        if (conn.responseCode !in 200..299) return null
        val body = readBody(conn)
        val files = org.json.JSONObject(body).getJSONArray("files")
        return if (files.length() > 0) files.getJSONObject(0).getString("id") else null
    }

    private fun readBody(conn: HttpURLConnection): String {
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return BufferedReader(InputStreamReader(stream)).use { it.readText() }
    }
}
