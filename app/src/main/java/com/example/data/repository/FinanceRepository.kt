package com.example.data.repository

import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val database: AppDatabase) {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val investmentDao = database.investmentDao()
    val backupManager = BackupManager(database)

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insert(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.update(transaction.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteById(id)
    }

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(type)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insert(category)
    }

    suspend fun deleteCategory(id: Long) {
        categoryDao.deleteCustomCategoryById(id)
    }

    // Budgets
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    suspend fun setBudget(category: String, limit: Double): Long {
        return budgetDao.insertOrUpdate(
            BudgetEntity(
                category = category,
                monthlyLimit = limit
            )
        )
    }

    suspend fun deleteBudget(id: Long) {
        budgetDao.deleteById(id)
    }

    // Savings Goals
    val allGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()

    suspend fun insertGoal(goal: SavingsGoalEntity): Long {
        return savingsGoalDao.insert(goal)
    }

    suspend fun updateGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.update(goal)
    }

    suspend fun deleteGoal(id: Long) {
        savingsGoalDao.deleteById(id)
    }

    // Investments
    val allInvestments: Flow<List<InvestmentEntity>> = investmentDao.getAllInvestments()

    suspend fun insertInvestment(investment: InvestmentEntity): Long {
        return investmentDao.insert(investment)
    }

    suspend fun updateInvestment(investment: InvestmentEntity) {
        investmentDao.update(investment)
    }

    suspend fun deleteInvestment(id: Long) {
        investmentDao.deleteById(id)
    }

    // Reset All Data
    suspend fun resetAllData() {
        transactionDao.deleteAll()
        budgetDao.deleteAll()
        savingsGoalDao.deleteAll()
        investmentDao.deleteAll()
        categoryDao.deleteAll()
        AppDatabase.populateInitialCategories(categoryDao)
    }
}
