package com.famly.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.famly.app.domain.FamlyAccess
import com.famly.app.ui.FamlyViewModel
import com.famly.app.ui.components.AppHeader
import com.famly.app.ui.components.FamlyBottomNav
import com.famly.app.ui.components.HeaderLeftSlot
import com.famly.app.ui.components.HeaderRightSlot
import com.famly.app.ui.screens.AccountsScreen
import com.famly.app.ui.screens.AnalyticsScreen
import com.famly.app.ui.screens.BackupScreen
import com.famly.app.ui.screens.BalancesScreen
import com.famly.app.ui.screens.BudgetScreen
import com.famly.app.ui.screens.CategoriesScreen
import com.famly.app.ui.screens.CategoryBudgetScreen
import com.famly.app.ui.screens.FamilyMemberScreen
import com.famly.app.ui.screens.FamilyScreen
import com.famly.app.ui.screens.HomeScreen
import com.famly.app.ui.screens.MoreScreen
import com.famly.app.ui.screens.OnboardingScreen
import com.famly.app.ui.screens.OperationDetailScreen
import com.famly.app.ui.screens.OperationsScreen
import com.famly.app.ui.screens.PremiumPaywallScreen
import com.famly.app.ui.screens.NotificationsSheet
import com.famly.app.ui.screens.QuickAddSheet
import com.famly.app.ui.screens.RecurringScreen
import com.famly.app.ui.screens.ReportsScreen
import com.famly.app.ui.screens.SettingsScreen
import com.famly.app.ui.screens.SplitExpenseScreen

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Routes.HOME, "Главная", Icons.Default.Home),
    TabItem(Routes.OPERATIONS, "Операции", Icons.Default.List),
    TabItem(Routes.BUDGET, "Бюджет", Icons.Default.AccountBalance),
    TabItem(Routes.MORE, "Ещё", Icons.Default.Menu),
)

@Composable
fun FamlyNavHost(
    viewModel: FamlyViewModel,
    pendingQuickAddType: String? = null,
    pendingJoinCode: String? = null,
    onIntentHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()
    var quickAddVisible by rememberSaveable { mutableStateOf(false) }
    var quickAddCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    val syncStatus by viewModel.syncStatus.collectAsState()
    val inviteCode by viewModel.inviteCode.collectAsState()
    val inviteLoading by viewModel.inviteLoading.collectAsState()
    val inviteError by viewModel.inviteError.collectAsState()
    var notificationsVisible by rememberSaveable { mutableStateOf(false) }
    var quickAddInitialType by rememberSaveable { mutableStateOf<String?>(null) }
    var familyJoinCode by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(pendingQuickAddType) {
        if (!pendingQuickAddType.isNullOrBlank()) {
            quickAddInitialType = pendingQuickAddType
            quickAddVisible = true
            onIntentHandled()
        }
    }

    LaunchedEffect(pendingJoinCode) {
        if (!pendingJoinCode.isNullOrBlank()) {
            familyJoinCode = pendingJoinCode
            navController.navigate(Routes.FAMILY)
            onIntentHandled()
        }
    }

    val syncMessage = syncStatus?.let {
        when {
            it.success && it.pulledCount + it.pushedCount > 0 ->
                "Синхронизировано: отправлено ${it.pushedCount}, получено ${it.pulledCount}"
            it.success -> "Синхронизация выполнена"
            it.error?.contains("Push failed", true) == true -> "Не удалось отправить данные на сервер"
            else -> it.error ?: "Ошибка синхронизации"
        }
    }

    if (!state.settings.onboardingComplete) {
        OnboardingScreen(onComplete = { viewModel.completeOnboarding() })
        return
    }

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route
    val isMainTab = tabs.any { it.route == currentRoute }

    fun navigateToPremium() {
        navController.navigate(Routes.PREMIUM)
    }

    fun openQuickAdd(categoryId: String? = null) {
        quickAddCategoryId = categoryId
        quickAddVisible = true
    }

    val headerLeft = when (currentRoute) {
        Routes.HOME, Routes.OPERATIONS, Routes.BUDGET, Routes.MORE -> HeaderLeftSlot.Notifications
        else -> HeaderLeftSlot.None
    }
    val headerRight = when (currentRoute) {
        Routes.HOME, Routes.OPERATIONS -> HeaderRightSlot.QuickAdd
        Routes.BUDGET -> HeaderRightSlot.Add
        Routes.MORE -> HeaderRightSlot.Settings
        else -> HeaderRightSlot.None
    }

    Scaffold(
        topBar = {
            if (isMainTab) {
                AppHeader(
                    leftSlot = headerLeft,
                    rightSlot = headerRight,
                    onQuickAdd = { openQuickAdd() },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onAdd = { navController.navigate(Routes.CATEGORIES) },
                    onNotifications = { notificationsVisible = true },
                    onHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (isMainTab) {
                FamlyBottomNav(
                    selectedRoute = currentRoute ?: Routes.HOME,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    tabs = tabs.map { Triple(it.route, it.icon, it.label) },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    state = state,
                    onOpenBudget = { navController.navigate(Routes.BUDGET) },
                    onOpenOperations = { navController.navigate(Routes.OPERATIONS) },
                    onOpenTransaction = { navController.navigate(Routes.operationDetail(it)) },
                    onQuickAddCategory = { openQuickAdd(it) },
                )
            }
            composable(Routes.OPERATIONS) {
                OperationsScreen(state) { navController.navigate(Routes.operationDetail(it)) }
            }
            composable(Routes.BUDGET) {
                BudgetScreen(
                    state,
                    { navController.navigate(Routes.budgetCategory(it)) },
                    { navController.navigate(Routes.CATEGORIES) },
                )
            }
            composable(Routes.MORE) {
                MoreScreen(
                    state = state,
                    onNavigate = { route -> navController.navigate(route) },
                    onOpenPremium = { navigateToPremium() },
                )
            }
            composable(Routes.ACCOUNTS) {
                AccountsScreen(
                    state,
                    { navController.popBackStack() },
                    { name, icon -> viewModel.addAccount(name, icon) },
                    { viewModel.deleteAccount(it) },
                    { viewModel.cycleAccountIcon(it) },
                )
            }
            composable(Routes.REPORTS) {
                ReportsScreen(state) { navController.popBackStack() }
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    state,
                    { navController.popBackStack() },
                    { viewModel.setTheme(it) },
                    { viewModel.setBudgetStartDay(it) },
                    { viewModel.setCurrency(it) },
                    syncMessage,
                    { email, password -> viewModel.login(email, password) },
                    { email, password, name -> viewModel.register(email, password, name) },
                    { viewModel.syncNow() },
                    { navController.navigate(Routes.FAMILY) },
                )
            }
            composable(Routes.RECURRING) {
                RecurringScreen(
                    state,
                    { navController.popBackStack() },
                    { viewModel.disableRecurring(it) },
                )
            }
            composable(Routes.BACKUP) {
                BackupScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onExportJson = { viewModel.exportBackupJson() },
                    onExportCsv = { period -> viewModel.exportCsv(period) },
                    onExportExcel = { period -> viewModel.exportExcel(period) },
                )
            }
            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    state,
                    { navController.popBackStack() },
                    { n, t, icon, color -> viewModel.addCategory(n, t, icon, color) },
                    { viewModel.deleteCategory(it) },
                    { viewModel.cycleCategoryIcon(it) },
                )
            }
            composable(Routes.PREMIUM) {
                PremiumPaywallScreen(
                    state,
                    { navController.popBackStack() },
                    {
                        viewModel.purchasePremium()
                        navController.popBackStack()
                    },
                )
            }
            composable(Routes.FAMILY) {
                FamilyScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onUpgrade = { navigateToPremium() },
                    onOpenMember = { navController.navigate(Routes.familyMember(it)) },
                    onSetupFamily = { viewModel.setupFamily(it) },
                    onJoinHousehold = { viewModel.joinHousehold(it) },
                    onRefreshInvite = { viewModel.generateInvite() },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    inviteCode = inviteCode,
                    inviteUrl = viewModel.inviteUrl(),
                    inviteLoading = inviteLoading,
                    inviteError = inviteError,
                    initialJoinCode = familyJoinCode,
                )
            }
            composable(
                Routes.FAMILY_MEMBER,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val memberId = entry.arguments?.getString("id") ?: return@composable
                FamilyMemberScreen(
                    state,
                    memberId,
                    { navController.popBackStack() },
                    { viewModel.updateFamilyMember(memberId, role = it) },
                    { viewModel.updateFamilyMember(memberId, visibility = it) },
                    { viewModel.cycleMemberAvatar(memberId) },
                )
            }
            composable(Routes.BALANCES) {
                BalancesScreen(
                    state,
                    { navController.popBackStack() },
                    { navigateToPremium() },
                    { from, to -> viewModel.settleIouBetween(from, to) },
                )
            }
            composable(Routes.ANALYTICS) {
                AnalyticsScreen(
                    state,
                    { navController.popBackStack() },
                    { navigateToPremium() },
                )
            }
            composable(
                Routes.OPERATION_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val txId = entry.arguments?.getString("id") ?: return@composable
                OperationDetailScreen(
                    state,
                    txId,
                    { navController.popBackStack() },
                    {
                        viewModel.deleteTransaction(txId)
                        navController.popBackStack()
                    },
                    {
                        if (FamlyAccess.hasPremium(state.settings)) {
                            navController.navigate(Routes.split(txId))
                        } else {
                            navigateToPremium()
                        }
                    },
                    { recurring, day -> viewModel.updateTransactionRecurring(txId, recurring, day) },
                )
            }
            composable(
                Routes.SPLIT,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val txId = entry.arguments?.getString("id") ?: return@composable
                SplitExpenseScreen(
                    state,
                    txId,
                    { navController.popBackStack() },
                    { navigateToPremium() },
                    { members ->
                        viewModel.saveSplit(txId, members)
                        navController.popBackStack()
                    },
                )
            }
            composable(
                Routes.BUDGET_CATEGORY,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val catId = entry.arguments?.getString("id") ?: return@composable
                CategoryBudgetScreen(
                    state,
                    catId,
                    { navController.popBackStack() },
                    { viewModel.updateCategoryBudget(catId, it) },
                    { viewModel.updateCategoryRollover(catId, it) },
                )
            }
        }
    }

    NotificationsSheet(
        state = state,
        visible = notificationsVisible,
        onDismiss = { notificationsVisible = false },
        onDismissNotice = { viewModel.dismissNotification(it) },
    )

    QuickAddSheet(
        state = state,
        visible = quickAddVisible,
        initialCategoryId = quickAddCategoryId,
        initialType = quickAddInitialType,
        onDismiss = {
            quickAddVisible = false
            quickAddCategoryId = null
            quickAddInitialType = null
        },
        onSave = { amount, type, cat, acc, note, rec ->
            viewModel.addTransaction(amount, type, cat, acc, note, rec)
        },
    )
}
