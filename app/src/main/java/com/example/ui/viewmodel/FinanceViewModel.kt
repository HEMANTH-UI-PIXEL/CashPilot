package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.DateRangeFilter
import com.example.data.model.FinancialSummary
import com.example.data.model.SortOrder
import com.example.data.model.TransactionType
import com.example.data.preferences.UserPreferences
import com.example.data.repository.FinanceRepository
import com.example.ui.components.ChartSlice
import com.example.ui.components.MonthlyBarData
import com.example.ui.components.getCategoryVisuals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(database)
    val preferences = UserPreferences(application)

    // User preferences & app lock state
    private val _currency = MutableStateFlow(preferences.getSelectedCurrency())
    val currency: StateFlow<Currency> = _currency.asStateFlow()

    private val _isAppLocked = MutableStateFlow(preferences.isPinLockEnabled)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // Transient message / snackbar
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Search, Filter & Sort states
    val searchQuery = MutableStateFlow("")
    val selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedDateFilter = MutableStateFlow(DateRangeFilter.ALL)
    val selectedSortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)

    // Calendar for Monthly Summary & Analytics (0 = Current Month, -1 = Previous Month, etc.)
    val selectedMonthOffset = MutableStateFlow(0)

    // Core flows
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<SavingsGoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvestments: StateFlow<List<InvestmentEntity>> = repository.allInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall Financial Summary: Total Income - Total Expenses - Total Savings - Total Investments
    val overallSummary: StateFlow<FinancialSummary> = allTransactions.combine(MutableStateFlow(Unit)) { txs, _ ->
        var income = 0.0
        var expense = 0.0
        var saving = 0.0
        var investment = 0.0

        for (tx in txs) {
            when (TransactionType.fromId(tx.type)) {
                TransactionType.INCOME -> income += tx.amount
                TransactionType.EXPENSE -> expense += tx.amount
                TransactionType.SAVING -> saving += tx.amount
                TransactionType.INVESTMENT -> investment += tx.amount
            }
        }
        FinancialSummary(income, expense, saving, investment)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // Current Month Summary
    val currentMonthSummary: StateFlow<FinancialSummary> = allTransactions.combine(MutableStateFlow(Unit)) { txs, _ ->
        val (startOfMonth, endOfMonth) = getMonthBounds(0)
        var income = 0.0
        var expense = 0.0
        var saving = 0.0
        var investment = 0.0

        for (tx in txs) {
            if (tx.date in startOfMonth..endOfMonth) {
                when (TransactionType.fromId(tx.type)) {
                    TransactionType.INCOME -> income += tx.amount
                    TransactionType.EXPENSE -> expense += tx.amount
                    TransactionType.SAVING -> saving += tx.amount
                    TransactionType.INVESTMENT -> investment += tx.amount
                }
            }
        }
        FinancialSummary(income, expense, saving, investment)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    data class FilterState(
        val query: String,
        val typeFilter: TransactionType?,
        val catFilter: String?,
        val dateFilter: DateRangeFilter,
        val sortOrder: SortOrder
    )

    private val filterState = combine(
        searchQuery,
        selectedTypeFilter,
        selectedCategoryFilter,
        selectedDateFilter,
        selectedSortOrder
    ) { query, typeFilter, catFilter, dateFilter, sortOrder ->
        FilterState(query, typeFilter, catFilter, dateFilter, sortOrder)
    }

    // Filtered and Sorted Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        filterState
    ) { txs, filter ->
        var list = txs

        // Type filter
        if (filter.typeFilter != null) {
            list = list.filter { it.type.equals(filter.typeFilter.id, ignoreCase = true) }
        }

        // Category filter
        if (!filter.catFilter.isNullOrBlank()) {
            list = list.filter { it.category.equals(filter.catFilter, ignoreCase = true) }
        }

        // Date range filter
        if (filter.dateFilter != DateRangeFilter.ALL) {
            val (start, end) = when (filter.dateFilter) {
                DateRangeFilter.THIS_MONTH -> getMonthBounds(0)
                DateRangeFilter.LAST_MONTH -> getMonthBounds(-1)
                DateRangeFilter.THIS_YEAR -> getYearBounds()
                else -> Pair(0L, Long.MAX_VALUE)
            }
            list = list.filter { it.date in start..end }
        }

        // Search query
        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.description.lowercase().contains(q)
            }
        }

        // Sorting
        when (filter.sortOrder) {
            SortOrder.NEWEST_FIRST -> list.sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.id })
            SortOrder.OLDEST_FIRST -> list.sortedWith(compareBy<TransactionEntity> { it.date }.thenBy { it.id })
            SortOrder.HIGHEST_AMOUNT -> list.sortedByDescending { it.amount }
            SortOrder.LOWEST_AMOUNT -> list.sortedBy { it.amount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Month for Monthly Summary Screen
    val selectedMonthTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        selectedMonthOffset
    ) { txs, offset ->
        val (start, end) = getMonthBounds(offset)
        txs.filter { it.date in start..end }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedMonthOffset(offset: Int) {
        selectedMonthOffset.value = offset
    }

    fun getMonthLabel(offset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    // Monthly Bar Chart Data (last 6 months)
    fun getMonthlyBarData(txs: List<TransactionEntity>): List<MonthlyBarData> {
        val result = mutableListOf<MonthlyBarData>()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            val monthLabel = monthFormat.format(cal.time)

            val (start, end) = getMonthBounds(-i)
            var inc = 0.0
            var exp = 0.0
            for (tx in txs) {
                if (tx.date in start..end) {
                    if (tx.type.equals("income", ignoreCase = true)) inc += tx.amount
                    if (tx.type.equals("expense", ignoreCase = true)) exp += tx.amount
                }
            }
            result.add(MonthlyBarData(monthLabel, inc, exp))
        }
        return result
    }

    // Expense Breakdown Slices for Donut Chart
    fun getExpenseBreakdownSlices(txs: List<TransactionEntity>): List<ChartSlice> {
        val expenseTxs = txs.filter { it.type.equals("expense", ignoreCase = true) }
        val categoryTotals = expenseTxs.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val total = categoryTotals.sumOf { it.second }
        if (total <= 0.0) return emptyList()

        return categoryTotals.take(6).map { (category, amount) ->
            val (_, _, _) = getCategoryVisuals(category, "expense")
            // Distinct pleasing slice colors
            val color = when (category.lowercase()) {
                "food" -> androidx.compose.ui.graphics.Color(0xFFEF4444)
                "transport" -> androidx.compose.ui.graphics.Color(0xFFF97316)
                "shopping" -> androidx.compose.ui.graphics.Color(0xFFEC4899)
                "bills" -> androidx.compose.ui.graphics.Color(0xFFEAB308)
                "rent" -> androidx.compose.ui.graphics.Color(0xFF8B5CF6)
                "entertainment" -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
                "health" -> androidx.compose.ui.graphics.Color(0xFF10B981)
                "education" -> androidx.compose.ui.graphics.Color(0xFF06B6D4)
                "subscriptions" -> androidx.compose.ui.graphics.Color(0xFF6366F1)
                "travel" -> androidx.compose.ui.graphics.Color(0xFF14B8A6)
                else -> androidx.compose.ui.graphics.Color(0xFF64748B)
            }
            ChartSlice(category, amount, color)
        }
    }

    // Operations
    fun addTransaction(
        type: String,
        amount: Double,
        title: String,
        category: String,
        date: Long,
        paymentMethod: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        if (amount <= 0.0) {
            emitMessage("Amount must be greater than zero.")
            return
        }
        if (title.isBlank()) {
            emitMessage("Title cannot be empty.")
            return
        }

        viewModelScope.launch {
            val entity = TransactionEntity(
                type = type.lowercase(),
                amount = amount,
                title = title.trim(),
                category = category.trim(),
                date = date,
                paymentMethod = paymentMethod,
                description = description.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertTransaction(entity)
            emitMessage("Transaction saved successfully!")
            onSuccess()
        }
    }

    fun updateTransaction(
        id: Long,
        type: String,
        amount: Double,
        title: String,
        category: String,
        date: Long,
        paymentMethod: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        if (amount <= 0.0) {
            emitMessage("Amount must be greater than zero.")
            return
        }
        if (title.isBlank()) {
            emitMessage("Title cannot be empty.")
            return
        }

        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id,
                type = type.lowercase(),
                amount = amount,
                title = title.trim(),
                category = category.trim(),
                date = date,
                paymentMethod = paymentMethod,
                description = description.trim(),
                updatedAt = System.currentTimeMillis()
            )
            repository.updateTransaction(entity)
            emitMessage("Transaction updated successfully!")
            onSuccess()
        }
    }

    fun deleteTransaction(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
            emitMessage("Transaction deleted.")
            onSuccess()
        }
    }

    // Category Management
    fun addCategory(name: String, type: String, iconName: String = "category") {
        if (name.isBlank()) {
            emitMessage("Category name cannot be empty.")
            return
        }
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    type = type.lowercase(),
                    iconName = iconName,
                    isDefault = false
                )
            )
            emitMessage("Category '${name.trim()}' added.")
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            emitMessage("Category deleted.")
        }
    }

    // Budget Management
    fun setBudget(category: String, limit: Double, onSuccess: () -> Unit) {
        if (limit <= 0.0) {
            emitMessage("Budget limit must be greater than zero.")
            return
        }
        viewModelScope.launch {
            repository.setBudget(category, limit)
            emitMessage("Budget set successfully.")
            onSuccess()
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
            emitMessage("Budget removed.")
        }
    }

    // Savings Goals
    fun addGoal(name: String, targetAmount: Double, currentAmount: Double, targetDate: Long, description: String, onSuccess: () -> Unit) {
        if (name.isBlank() || targetAmount <= 0.0) {
            emitMessage("Please enter a valid goal name and target amount.")
            return
        }
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoalEntity(
                    name = name.trim(),
                    targetAmount = targetAmount,
                    currentAmount = currentAmount.coerceAtLeast(0.0),
                    targetDate = targetDate,
                    description = description.trim(),
                    isCompleted = currentAmount >= targetAmount
                )
            )
            emitMessage("Savings goal '$name' created!")
            onSuccess()
        }
    }

    fun addMoneyToGoal(goal: SavingsGoalEntity, additionalAmount: Double, onSuccess: () -> Unit) {
        if (additionalAmount <= 0.0) {
            emitMessage("Enter a valid amount to contribute.")
            return
        }
        viewModelScope.launch {
            val newAmount = goal.currentAmount + additionalAmount
            val updated = goal.copy(
                currentAmount = newAmount,
                isCompleted = newAmount >= goal.targetAmount
            )
            repository.updateGoal(updated)
            emitMessage("Added ${currency.value.format(additionalAmount)} to ${goal.name}!")
            onSuccess()
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
            emitMessage("Goal deleted.")
        }
    }

    // Investment Tracker
    fun addInvestment(name: String, type: String, amount: Double, notes: String, onSuccess: () -> Unit) {
        if (name.isBlank() || amount <= 0.0) {
            emitMessage("Please enter a valid investment name and amount.")
            return
        }
        viewModelScope.launch {
            repository.insertInvestment(
                InvestmentEntity(
                    name = name.trim(),
                    type = type.trim(),
                    amountInvested = amount,
                    notes = notes.trim()
                )
            )
            emitMessage("Investment '$name' recorded!")
            onSuccess()
        }
    }

    fun deleteInvestment(id: Long) {
        viewModelScope.launch {
            repository.deleteInvestment(id)
            emitMessage("Investment removed.")
        }
    }

    // Settings: Currency
    fun setCurrency(newCurrency: Currency) {
        preferences.currencyCode = newCurrency.code
        _currency.value = newCurrency
        emitMessage("Currency updated to ${newCurrency.name}")
    }

    // Settings: PIN Security
    fun unlockApp(pin: String): Boolean {
        val success = preferences.verifyPin(pin)
        if (success) {
            _isAppLocked.value = false
        }
        return success
    }

    fun setAppPin(pin: String) {
        preferences.setPin(pin)
        emitMessage("App lock PIN enabled successfully.")
    }

    fun disableAppPin(currentPin: String): Boolean {
        val success = preferences.disablePin(currentPin)
        if (success) {
            _isAppLocked.value = false
            emitMessage("App lock PIN disabled.")
        } else {
            emitMessage("Incorrect current PIN.")
        }
        return success
    }

    // Reset All Data
    fun resetAllData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.resetAllData()
            emitMessage("All local data has been reset.")
            onSuccess()
        }
    }

    // Backup & Restore
    suspend fun exportDataJson(): String {
        return repository.backupManager.exportDataToJson()
    }

    fun importDataJson(json: String, onComplete: (BackupManager.ImportResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.backupManager.importDataFromJson(json)
            when (result) {
                is BackupManager.ImportResult.Success -> {
                    emitMessage("Import successful: ${result.transactionCount} transactions restored.")
                }
                is BackupManager.ImportResult.Error -> {
                    emitMessage(result.message)
                }
            }
            onComplete(result)
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }

    private fun getMonthBounds(monthOffset: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, monthOffset)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    private fun getYearBounds(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}
