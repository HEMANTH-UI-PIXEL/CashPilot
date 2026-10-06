package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        InvestmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun investmentDao(): InvestmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moneypilot_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialCategories(database.categoryDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (database.categoryDao().getCount() == 0) {
                            populateInitialCategories(database.categoryDao())
                        }
                    }
                }
            }
        }

        suspend fun populateInitialCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                // Income
                CategoryEntity(name = "Salary", type = "income", iconName = "payments", isDefault = true),
                CategoryEntity(name = "Freelance", type = "income", iconName = "work", isDefault = true),
                CategoryEntity(name = "Business", type = "income", iconName = "storefront", isDefault = true),
                CategoryEntity(name = "Interest", type = "income", iconName = "trending_up", isDefault = true),
                CategoryEntity(name = "Gift", type = "income", iconName = "card_giftcard", isDefault = true),
                CategoryEntity(name = "Other Income", type = "income", iconName = "account_balance_wallet", isDefault = true),

                // Expenses
                CategoryEntity(name = "Food", type = "expense", iconName = "restaurant", isDefault = true),
                CategoryEntity(name = "Transport", type = "expense", iconName = "directions_car", isDefault = true),
                CategoryEntity(name = "Shopping", type = "expense", iconName = "shopping_bag", isDefault = true),
                CategoryEntity(name = "Bills", type = "expense", iconName = "receipt_long", isDefault = true),
                CategoryEntity(name = "Rent", type = "expense", iconName = "home", isDefault = true),
                CategoryEntity(name = "Entertainment", type = "expense", iconName = "movie", isDefault = true),
                CategoryEntity(name = "Health", type = "expense", iconName = "local_hospital", isDefault = true),
                CategoryEntity(name = "Education", type = "expense", iconName = "school", isDefault = true),
                CategoryEntity(name = "Subscriptions", type = "expense", iconName = "subscriptions", isDefault = true),
                CategoryEntity(name = "Travel", type = "expense", iconName = "flight", isDefault = true),
                CategoryEntity(name = "Other Expense", type = "expense", iconName = "more_horiz", isDefault = true),

                // Savings
                CategoryEntity(name = "Emergency Fund", type = "saving", iconName = "shield", isDefault = true),
                CategoryEntity(name = "General Savings", type = "saving", iconName = "savings", isDefault = true),
                CategoryEntity(name = "Goal Savings", type = "saving", iconName = "flag", isDefault = true),
                CategoryEntity(name = "Other Savings", type = "saving", iconName = "account_balance", isDefault = true),

                // Investments
                CategoryEntity(name = "Mutual Funds", type = "investment", iconName = "pie_chart", isDefault = true),
                CategoryEntity(name = "Stocks", type = "investment", iconName = "show_chart", isDefault = true),
                CategoryEntity(name = "Fixed Deposit", type = "investment", iconName = "lock_clock", isDefault = true),
                CategoryEntity(name = "Gold", type = "investment", iconName = "monetization_on", isDefault = true),
                CategoryEntity(name = "Bonds", type = "investment", iconName = "description", isDefault = true),
                CategoryEntity(name = "Other Investment", type = "investment", iconName = "diamond", isDefault = true)
            )
            categoryDao.insertAll(defaultCategories)
        }
    }
}
