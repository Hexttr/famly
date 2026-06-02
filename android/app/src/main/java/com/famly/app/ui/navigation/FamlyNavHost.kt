package com.famly.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.famly.app.ui.FamlyViewModel
import com.famly.app.ui.screens.AccountsScreen
import com.famly.app.ui.screens.AnalyticsScreen
import com.famly.app.ui.screens.BackupScreen
import com.famly.app.ui.screens.BalancesScreen
import com.famly.app.ui.screens.BudgetScreen
import com.famly.app.ui.screens.CategoriesScreen
import com.famly.app.ui.screens.CategoryBudgetScreen
import com.famly.app.ui.screens.FamilyScreen
import com.famly.app.ui.screens.HomeScreen
import com.famly.app.ui.screens.MoreScreen
import com.famly.app.ui.screens.OnboardingScreen
import com.famly.app.ui.screens.OperationDetailScreen
import com.famly.app.ui.screens.OperationsScreen
import com.famly.app.ui.screens.PremiumPaywallScreen
import com.famly.app.ui.screens.QuickAddSheet
import com.famly.app.ui.screens.ReportsScreen
import com.famly.app.ui.screens.SettingsScreen
import com.famly.app.ui.theme.Primary

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Routes.HOME, "Главная", Icons.Default.Home),
    TabItem(Routes.OPERATIONS, "Операции", Icons.Default.List),
    TabItem(Routes.BUDGET, "Бюджет", Icons.Default.AccountBalance),
    TabItem(Routes.MORE, "Ещё", Icons.Default.Menu),
)

@Composable
fun FamlyNavHost(viewModel: FamlyViewModel) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()
    var quickAddVisible by rememberSaveable { mutableStateOf(false) }

    if (!state.settings.onboardingComplete) {
        OnboardingScreen(onComplete = { viewModel.completeOnboarding() })
        return
    }

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route
    val isMainTab = tabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isMainTab) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigate(tab.route) { popUpTo(Routes.HOME); launchSingleTop = true } },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isMainTab) {
                FloatingActionButton(onClick = { quickAddVisible = true }, containerColor = Primary) {
                    Text("+")
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(state, { navController.navigate(Routes.OPERATIONS) }, { navController.navigate(Routes.operationDetail(it)) })
            }
            composable(Routes.OPERATIONS) {
                OperationsScreen(state) { navController.navigate(Routes.operationDetail(it)) }
            }
            composable(Routes.BUDGET) {
                BudgetScreen(state, { navController.navigate(Routes.budgetCategory(it)) }, { navController.navigate(Routes.CATEGORIES) })
            }
            composable(Routes.MORE) {
                MoreScreen(state) { route -> navController.navigate(route) }
            }
            composable(Routes.ACCOUNTS) {
                AccountsScreen(state, { navController.popBackStack() }, { viewModel.addAccount(it) }, { viewModel.deleteAccount(it) })
            }
            composable(Routes.REPORTS) {
                ReportsScreen(state) { navController.popBackStack() }
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(state, { navController.popBackStack() }, { viewModel.setTheme(it) }, { viewModel.setBudgetStartDay(it) })
            }
            composable(Routes.BACKUP) {
                BackupScreen { navController.popBackStack() }
            }
            composable(Routes.CATEGORIES) {
                CategoriesScreen(state, { navController.popBackStack() }, { n, t -> viewModel.addCategory(n, t) }, { viewModel.deleteCategory(it) })
            }
            composable(Routes.PREMIUM) {
                PremiumPaywallScreen(state, { navController.popBackStack() }, { viewModel.activatePremium(); navController.popBackStack() })
            }
            composable(Routes.FAMILY) {
                FamilyScreen(state, { navController.popBackStack() }) { navController.navigate(Routes.PREMIUM) }
            }
            composable(Routes.BALANCES) {
                BalancesScreen(state, { navController.popBackStack() }) { navController.navigate(Routes.PREMIUM) }
            }
            composable(Routes.ANALYTICS) {
                AnalyticsScreen(state, { navController.popBackStack() }) { navController.navigate(Routes.PREMIUM) }
            }
            composable(Routes.OPERATION_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                OperationDetailScreen(
                    state,
                    entry.arguments?.getString("id") ?: return@composable,
                    { navController.popBackStack() },
                    { viewModel.deleteTransaction(entry.arguments?.getString("id") ?: return@OperationDetailScreen); navController.popBackStack() },
                    { navController.navigate(Routes.PREMIUM) },
                )
            }
            composable(Routes.BUDGET_CATEGORY, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                CategoryBudgetScreen(state, entry.arguments?.getString("id") ?: return@composable, { navController.popBackStack() }) {
                    viewModel.updateCategoryBudget(entry.arguments?.getString("id") ?: return@CategoryBudgetScreen, it)
                }
            }
        }
    }

    QuickAddSheet(
        state = state,
        visible = quickAddVisible,
        onDismiss = { quickAddVisible = false },
        onSave = { amount, type, cat, acc, note, rec ->
            viewModel.addTransaction(amount, type, cat, acc, note, rec)
        },
    )
}
