package com.example.data.backup

import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.InvestmentEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(private val database: AppDatabase) {

    suspend fun exportDataToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "MoneyPilot")
        root.put("version", 1)
        root.put("exported_at", System.currentTimeMillis())

        // Transactions
        val transactions = database.transactionDao().getAllTransactionsSnapshot()
        val txArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("type", tx.type)
                put("amount", tx.amount)
                put("category", tx.category)
                put("title", tx.title)
                put("description", tx.description)
                put("date", tx.date)
                put("payment_method", tx.paymentMethod)
                put("created_at", tx.createdAt)
                put("updated_at", tx.updatedAt)
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // Categories
        val categories = database.categoryDao().getAllCategoriesSnapshot()
        val catArray = JSONArray()
        for (cat in categories) {
            val obj = JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("type", cat.type)
                put("icon_name", cat.iconName)
                put("is_default", cat.isDefault)
            }
            catArray.put(obj)
        }
        root.put("categories", catArray)

        // Budgets
        val budgets = database.budgetDao().getAllBudgetsSnapshot()
        val budgetArray = JSONArray()
        for (b in budgets) {
            val obj = JSONObject().apply {
                put("id", b.id)
                put("category", b.category)
                put("monthly_limit", b.monthlyLimit)
                put("created_at", b.createdAt)
            }
            budgetArray.put(obj)
        }
        root.put("budgets", budgetArray)

        // Savings Goals
        val goals = database.savingsGoalDao().getAllGoalsSnapshot()
        val goalArray = JSONArray()
        for (g in goals) {
            val obj = JSONObject().apply {
                put("id", g.id)
                put("name", g.name)
                put("target_amount", g.targetAmount)
                put("current_amount", g.currentAmount)
                put("target_date", g.targetDate)
                put("description", g.description)
                put("is_completed", g.isCompleted)
                put("created_at", g.createdAt)
            }
            goalArray.put(obj)
        }
        root.put("savings_goals", goalArray)

        // Investments
        val investments = database.investmentDao().getAllInvestmentsSnapshot()
        val invArray = JSONArray()
        for (inv in investments) {
            val obj = JSONObject().apply {
                put("id", inv.id)
                put("name", inv.name)
                put("type", inv.type)
                put("amount_invested", inv.amountInvested)
                put("date", inv.date)
                put("notes", inv.notes)
                put("created_at", inv.createdAt)
            }
            invArray.put(obj)
        }
        root.put("investments", invArray)

        root.toString(2)
    }

    sealed class ImportResult {
        data class Success(val transactionCount: Int, val goalCount: Int, val investmentCount: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    suspend fun importDataFromJson(jsonString: String): ImportResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("transactions")) {
                return@withContext ImportResult.Error("Invalid backup format: missing transactions data")
            }

            // Parse transactions
            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            val txList = mutableListOf<TransactionEntity>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val amount = obj.optDouble("amount", 0.0)
                val title = obj.optString("title", "").trim()
                val type = obj.optString("type", "expense").lowercase()

                if (amount <= 0 || title.isEmpty()) {
                    continue // Skip invalid item without corrupting others
                }

                txList.add(
                    TransactionEntity(
                        id = 0, // Auto-generate clean primary keys to avoid conflicts
                        type = type,
                        amount = amount,
                        category = obj.optString("category", "Other Expense"),
                        title = title,
                        description = obj.optString("description", ""),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        paymentMethod = obj.optString("payment_method", "Cash"),
                        createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updated_at", System.currentTimeMillis())
                    )
                )
            }

            // Parse categories
            val catArray = root.optJSONArray("categories")
            val catList = mutableListOf<CategoryEntity>()
            if (catArray != null) {
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    val name = obj.optString("name", "").trim()
                    if (name.isNotEmpty()) {
                        catList.add(
                            CategoryEntity(
                                id = 0,
                                name = name,
                                type = obj.optString("type", "expense"),
                                iconName = obj.optString("icon_name", "category"),
                                isDefault = obj.optBoolean("is_default", false)
                            )
                        )
                    }
                }
            }

            // Parse budgets
            val budgetArray = root.optJSONArray("budgets")
            val budgetList = mutableListOf<BudgetEntity>()
            if (budgetArray != null) {
                for (i in 0 until budgetArray.length()) {
                    val obj = budgetArray.getJSONObject(i)
                    val limit = obj.optDouble("monthly_limit", 0.0)
                    val cat = obj.optString("category", "").trim()
                    if (limit > 0 && cat.isNotEmpty()) {
                        budgetList.add(
                            BudgetEntity(
                                id = 0,
                                category = cat,
                                monthlyLimit = limit,
                                createdAt = obj.optLong("created_at", System.currentTimeMillis())
                            )
                        )
                    }
                }
            }

            // Parse savings goals
            val goalArray = root.optJSONArray("savings_goals")
            val goalList = mutableListOf<SavingsGoalEntity>()
            if (goalArray != null) {
                for (i in 0 until goalArray.length()) {
                    val obj = goalArray.getJSONObject(i)
                    val name = obj.optString("name", "").trim()
                    val target = obj.optDouble("target_amount", 0.0)
                    if (name.isNotEmpty() && target > 0) {
                        goalList.add(
                            SavingsGoalEntity(
                                id = 0,
                                name = name,
                                targetAmount = target,
                                currentAmount = obj.optDouble("current_amount", 0.0),
                                targetDate = obj.optLong("target_date", 0L),
                                description = obj.optString("description", ""),
                                isCompleted = obj.optBoolean("is_completed", false),
                                createdAt = obj.optLong("created_at", System.currentTimeMillis())
                            )
                        )
                    }
                }
            }

            // Parse investments
            val invArray = root.optJSONArray("investments")
            val invList = mutableListOf<InvestmentEntity>()
            if (invArray != null) {
                for (i in 0 until invArray.length()) {
                    val obj = invArray.getJSONObject(i)
                    val name = obj.optString("name", "").trim()
                    val amount = obj.optDouble("amount_invested", 0.0)
                    if (name.isNotEmpty() && amount > 0) {
                        invList.add(
                            InvestmentEntity(
                                id = 0,
                                name = name,
                                type = obj.optString("type", "Other"),
                                amountInvested = amount,
                                date = obj.optLong("date", System.currentTimeMillis()),
                                notes = obj.optString("notes", ""),
                                createdAt = obj.optLong("created_at", System.currentTimeMillis())
                            )
                        )
                    }
                }
            }

            // Save to database
            if (txList.isNotEmpty()) {
                database.transactionDao().insertAll(txList)
            }
            if (catList.isNotEmpty()) {
                database.categoryDao().insertAll(catList)
            }
            if (budgetList.isNotEmpty()) {
                database.budgetDao().insertAll(budgetList)
            }
            if (goalList.isNotEmpty()) {
                database.savingsGoalDao().insertAll(goalList)
            }
            if (invList.isNotEmpty()) {
                database.investmentDao().insertAll(invList)
            }

            ImportResult.Success(txList.size, goalList.size, invList.size)
        } catch (e: Exception) {
            ImportResult.Error("Failed to parse backup data: ${e.localizedMessage ?: "Unknown format error"}")
        }
    }
}
