package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.screens.AddEditTransactionSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvestmentsScreen
import com.example.ui.screens.MonthlySummaryScreen
import com.example.ui.screens.MoreDestination
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.collectLatest

enum class MainTab {
    HOME,
    TRANSACTIONS,
    ANALYTICS,
    GOALS,
    MORE
}

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MoneyPilotApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyPilotApp(viewModel: FinanceViewModel) {
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()

    // If app is locked by PIN, show PIN Keypad
    if (isAppLocked) {
        PinLockScreen(
            onUnlockAttempt = { pin -> viewModel.unlockApp(pin) }
        )
        return
    }

    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var subDestination by remember { mutableStateOf<MoreDestination?>(null) }

    // Bottom sheet state for Add/Edit Transaction
    var showAddSheet by remember { mutableStateOf(false) }
    var sheetInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to ViewModel transient messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Observe data
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val allInvestments by viewModel.allInvestments.collectAsStateWithLifecycle()

    val overallSummary by viewModel.overallSummary.collectAsStateWithLifecycle()
    val currentMonthSummary by viewModel.currentMonthSummary.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsStateWithLifecycle()
    val selectedSortOrder by viewModel.selectedSortOrder.collectAsStateWithLifecycle()

    val monthOffset by viewModel.selectedMonthOffset.collectAsStateWithLifecycle()
    val selectedMonthTransactions by viewModel.selectedMonthTransactions.collectAsStateWithLifecycle()

    // Back handling for sub-destinations
    BackHandler(enabled = subDestination != null) {
        subDestination = null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (selectedTab == MainTab.MORE && subDestination != null) {
                val title = when (subDestination) {
                    MoreDestination.BUDGETS -> "Budgets"
                    MoreDestination.INVESTMENTS -> "Investments"
                    MoreDestination.MONTHLY_SUMMARY -> "Monthly Report"
                    MoreDestination.SETTINGS -> "Settings"
                    null -> ""
                }
                TopAppBar(
                    title = { Text(text = title, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { subDestination = null }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Slate900
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (subDestination == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == MainTab.HOME,
                        onClick = { selectedTab = MainTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("bottom_nav_home"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate900,
                            selectedTextColor = Slate900,
                            indicatorColor = Slate200
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.TRANSACTIONS,
                        onClick = { selectedTab = MainTab.TRANSACTIONS },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transactions") },
                        label = { Text("Transactions") },
                        modifier = Modifier.testTag("bottom_nav_transactions"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate900,
                            selectedTextColor = Slate900,
                            indicatorColor = Slate200
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.ANALYTICS,
                        onClick = { selectedTab = MainTab.ANALYTICS },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                        label = { Text("Analytics") },
                        modifier = Modifier.testTag("bottom_nav_analytics"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate900,
                            selectedTextColor = Slate900,
                            indicatorColor = Slate200
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.GOALS,
                        onClick = { selectedTab = MainTab.GOALS },
                        icon = { Icon(Icons.Default.Flag, contentDescription = "Goals") },
                        label = { Text("Goals") },
                        modifier = Modifier.testTag("bottom_nav_goals"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate900,
                            selectedTextColor = Slate900,
                            indicatorColor = Slate200
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == MainTab.MORE,
                        onClick = {
                            selectedTab = MainTab.MORE
                            subDestination = null
                        },
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                        label = { Text("More") },
                        modifier = Modifier.testTag("bottom_nav_more"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Slate900,
                            selectedTextColor = Slate900,
                            indicatorColor = Slate200
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                MainTab.HOME -> {
                    DashboardScreen(
                        overallSummary = overallSummary,
                        currentMonthSummary = currentMonthSummary,
                        recentTransactions = allTransactions,
                        currency = currency,
                        onAddTransactionClick = { type ->
                            editingTransaction = null
                            sheetInitialType = type
                            showAddSheet = true
                        },
                        onTransactionClick = { tx ->
                            editingTransaction = tx
                            showAddSheet = true
                        },
                        onViewAllTransactionsClick = {
                            selectedTab = MainTab.TRANSACTIONS
                        }
                    )
                }

                MainTab.TRANSACTIONS -> {
                    TransactionsScreen(
                        transactions = filteredTransactions,
                        categories = allCategories,
                        currency = currency,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        selectedType = selectedTypeFilter,
                        onTypeFilterChange = { viewModel.selectedTypeFilter.value = it },
                        selectedCategory = selectedCategoryFilter,
                        onCategoryFilterChange = { viewModel.selectedCategoryFilter.value = it },
                        selectedDateFilter = selectedDateFilter,
                        onDateFilterChange = { viewModel.selectedDateFilter.value = it },
                        sortOrder = selectedSortOrder,
                        onSortOrderChange = { viewModel.selectedSortOrder.value = it },
                        onAddTransactionClick = {
                            editingTransaction = null
                            sheetInitialType = TransactionType.EXPENSE
                            showAddSheet = true
                        },
                        onEditTransaction = { tx ->
                            editingTransaction = tx
                            showAddSheet = true
                        },
                        onDeleteTransaction = { id ->
                            viewModel.deleteTransaction(id)
                        }
                    )
                }

                MainTab.ANALYTICS -> {
                    val slices = viewModel.getExpenseBreakdownSlices(allTransactions)
                    val barData = viewModel.getMonthlyBarData(allTransactions)
                    AnalyticsScreen(
                        allTransactions = allTransactions,
                        currency = currency,
                        donutSlices = slices,
                        monthlyBarData = barData
                    )
                }

                MainTab.GOALS -> {
                    SavingsGoalsScreen(
                        goals = allGoals,
                        currency = currency,
                        onAddGoal = { name, target, current, targetDate, desc ->
                            viewModel.addGoal(name, target, current, targetDate, desc) {}
                        },
                        onAddMoneyToGoal = { goal, amt ->
                            viewModel.addMoneyToGoal(goal, amt) {}
                        },
                        onDeleteGoal = { id ->
                            viewModel.deleteGoal(id)
                        }
                    )
                }

                MainTab.MORE -> {
                    when (subDestination) {
                        null -> {
                            MoreScreen(onNavigate = { dest -> subDestination = dest })
                        }
                        MoreDestination.BUDGETS -> {
                            BudgetsScreen(
                                budgets = allBudgets,
                                transactions = allTransactions,
                                categories = allCategories,
                                currency = currency,
                                onSetBudget = { cat, limit ->
                                    viewModel.setBudget(cat, limit) {}
                                },
                                onDeleteBudget = { id ->
                                    viewModel.deleteBudget(id)
                                }
                            )
                        }
                        MoreDestination.INVESTMENTS -> {
                            InvestmentsScreen(
                                investments = allInvestments,
                                currency = currency,
                                onAddInvestment = { name, type, amt, notes ->
                                    viewModel.addInvestment(name, type, amt, notes) {}
                                },
                                onDeleteInvestment = { id ->
                                    viewModel.deleteInvestment(id)
                                }
                            )
                        }
                        MoreDestination.MONTHLY_SUMMARY -> {
                            MonthlySummaryScreen(
                                transactions = selectedMonthTransactions,
                                monthLabel = viewModel.getMonthLabel(monthOffset),
                                currentOffset = monthOffset,
                                currency = currency,
                                onPreviousMonth = { viewModel.setSelectedMonthOffset(monthOffset - 1) },
                                onNextMonth = { viewModel.setSelectedMonthOffset(monthOffset + 1) }
                            )
                        }
                        MoreDestination.SETTINGS -> {
                            SettingsScreen(
                                currentCurrency = currency,
                                categories = allCategories,
                                userPreferences = viewModel.preferences,
                                onCurrencyChange = { newCurr -> viewModel.setCurrency(newCurr) },
                                onDeleteCustomCategory = { id -> viewModel.deleteCategory(id) },
                                onResetAllData = { viewModel.resetAllData {} },
                                onExportJson = { viewModel.exportDataJson() },
                                onImportJson = { json, cb -> viewModel.importDataJson(json, cb) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Add / Edit Transaction
    if (showAddSheet) {
        AddEditTransactionSheet(
            existingTransaction = editingTransaction,
            initialType = sheetInitialType,
            currency = currency,
            categories = allCategories,
            onDismiss = {
                showAddSheet = false
                editingTransaction = null
            },
            onSave = { type, amount, title, category, date, paymentMethod, description ->
                if (editingTransaction != null) {
                    viewModel.updateTransaction(
                        id = editingTransaction!!.id,
                        type = type,
                        amount = amount,
                        title = title,
                        category = category,
                        date = date,
                        paymentMethod = paymentMethod,
                        description = description,
                        onSuccess = {
                            showAddSheet = false
                            editingTransaction = null
                        }
                    )
                } else {
                    viewModel.addTransaction(
                        type = type,
                        amount = amount,
                        title = title,
                        category = category,
                        date = date,
                        paymentMethod = paymentMethod,
                        description = description,
                        onSuccess = {
                            showAddSheet = false
                        }
                    )
                }
            },
            onAddCustomCategory = { name, type ->
                viewModel.addCategory(name, type)
            }
        )
    }
}
