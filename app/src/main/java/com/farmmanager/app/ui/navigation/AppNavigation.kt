package com.farmmanager.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.farmmanager.app.data.repository.FarmRepository
import com.farmmanager.app.ui.backup.BackupViewModel
import com.farmmanager.app.ui.dashboard.DashboardScreen
import com.farmmanager.app.ui.dashboard.DashboardViewModel
import com.farmmanager.app.ui.egg.EggScreen
import com.farmmanager.app.ui.egg.EggViewModel
import com.farmmanager.app.ui.feed.FeedScreen
import com.farmmanager.app.ui.feed.FeedViewModel
import com.farmmanager.app.ui.flock.FlockScreen
import com.farmmanager.app.ui.flock.FlockViewModel
import com.farmmanager.app.ui.health.HealthScreen
import com.farmmanager.app.ui.health.HealthViewModel
import com.farmmanager.app.ui.more.MoreScreen
import com.farmmanager.app.ui.reminder.ReminderViewModel
import com.farmmanager.app.ui.reports.ReportsScreen
import com.farmmanager.app.ui.transaction.TransactionViewModel
import com.farmmanager.app.util.ViewModelFactory

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Flocks : Screen("flocks", "Flocks", Icons.Default.Pets)
    object Eggs : Screen("eggs", "Eggs", Icons.Default.Egg)
    object Reports : Screen("reports", "Reports", Icons.Default.Assessment)
    object More : Screen("more", "More", Icons.Default.MoreHoriz)
}

private val bottomNavItems = listOf(Screen.Dashboard, Screen.Flocks, Screen.Eggs, Screen.Reports, Screen.More)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(repository: FarmRepository) {
    val navController = rememberNavController()
    val appContext = LocalContext.current.applicationContext
    val factory = ViewModelFactory(repository, appContext)

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val flockViewModel: FlockViewModel = viewModel(factory = factory)
    val eggViewModel: EggViewModel = viewModel(factory = factory)
    val feedViewModel: FeedViewModel = viewModel(factory = factory)
    val healthViewModel: HealthViewModel = viewModel(factory = factory)
    val transactionViewModel: TransactionViewModel = viewModel(factory = factory)
    val reminderViewModel: ReminderViewModel = viewModel(factory = factory)
    val backupViewModel: BackupViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = { AppBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen(dashboardViewModel) }
            composable(Screen.Flocks.route) { FlockScreen(flockViewModel) }
            composable(Screen.Eggs.route) { EggScreen(eggViewModel, flockViewModel) }
            composable(Screen.Reports.route) {
                ReportsScreen(eggViewModel, feedViewModel, transactionViewModel, flockViewModel)
            }
            composable(Screen.More.route) {
                MoreScreen(
                    feedViewModel = feedViewModel,
                    healthViewModel = healthViewModel,
                    transactionViewModel = transactionViewModel,
                    reminderViewModel = reminderViewModel,
                    flockViewModel = flockViewModel,
                    backupViewModel = backupViewModel
                )
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
