package com.farmmanager.app.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.farmmanager.app.ui.backup.BackupScreen
import com.farmmanager.app.ui.backup.BackupViewModel
import com.farmmanager.app.ui.feed.FeedScreen
import com.farmmanager.app.ui.feed.FeedViewModel
import com.farmmanager.app.ui.flock.FlockViewModel
import com.farmmanager.app.ui.health.HealthScreen
import com.farmmanager.app.ui.health.HealthViewModel
import com.farmmanager.app.ui.reminder.ReminderScreen
import com.farmmanager.app.ui.reminder.ReminderViewModel
import com.farmmanager.app.ui.transaction.TransactionScreen
import com.farmmanager.app.ui.transaction.TransactionViewModel

private enum class MoreDestination(val title: String, val icon: ImageVector) {
    MENU("More", Icons.Default.MoreHoriz),
    FEED("Feed Management", Icons.Default.Grass),
    HEALTH("Health Records", Icons.Default.HealthAndSafety),
    TRANSACTIONS("Income & Expenses", Icons.Default.AttachMoney),
    REMINDERS("Reminders", Icons.Default.Alarm),
    BACKUP("Backup & Export", Icons.Default.CloudUpload)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    feedViewModel: FeedViewModel,
    healthViewModel: HealthViewModel,
    transactionViewModel: TransactionViewModel,
    reminderViewModel: ReminderViewModel,
    flockViewModel: FlockViewModel,
    backupViewModel: BackupViewModel
) {
    var current by remember { mutableStateOf(MoreDestination.MENU) }

    when (current) {
        MoreDestination.MENU -> MoreMenu(onSelect = { current = it })
        MoreDestination.FEED -> BackableScreen(current.title, { current = MoreDestination.MENU }) {
            FeedScreen(feedViewModel, flockViewModel)
        }
        MoreDestination.HEALTH -> BackableScreen(current.title, { current = MoreDestination.MENU }) {
            HealthScreen(healthViewModel, flockViewModel)
        }
        MoreDestination.TRANSACTIONS -> BackableScreen(current.title, { current = MoreDestination.MENU }) {
            TransactionScreen(transactionViewModel)
        }
        MoreDestination.REMINDERS -> BackableScreen(current.title, { current = MoreDestination.MENU }) {
            ReminderScreen(reminderViewModel)
        }
        MoreDestination.BACKUP -> BackableScreen(current.title, { current = MoreDestination.MENU }) {
            BackupScreen(backupViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreMenu(onSelect: (MoreDestination) -> Unit) {
    val items = listOf(
        MoreDestination.FEED,
        MoreDestination.HEALTH,
        MoreDestination.TRANSACTIONS,
        MoreDestination.REMINDERS,
        MoreDestination.BACKUP
    )

    Scaffold(topBar = { TopAppBar(title = { Text("More") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { destination ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(destination) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(destination.icon, contentDescription = destination.title)
                        Text(destination.title, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackableScreen(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    // The wrapped screens already provide their own Scaffold/TopAppBar with a title;
    // we layer a lightweight back affordance above them via BackHandler-less simple approach:
    // simplest reliable approach is to just show the content, since each screen has its own Scaffold.
    // We add a back button row on top for navigation back to the menu.
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to more menu")
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}
