package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.FinancialSummary
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: FinanceRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FinanceRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context matches MoneyPilot`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MoneyPilot", appName)
    }

    @Test
    fun `currency Indian numbering formatting works accurately`() {
        val inr = Currency.INR
        assertEquals("₹1,500", inr.format(1500.0))
        assertEquals("₹25,000", inr.format(25000.0))
        assertEquals("₹1,25,000", inr.format(125000.0))
        assertEquals("₹30,000", inr.format(30000.0))
    }

    @Test
    fun `verify full scenario workflow - insert, recalculate, update, delete, filter, and balance`() = runBlocking {
        // 1. Start with an empty database
        assertEquals(0, repository.allTransactions.first().size)

        // 2. Add salary income of ₹30,000
        val salaryId = repository.insertTransaction(
            TransactionEntity(
                type = "income",
                amount = 30000.0,
                title = "Salary",
                category = "Salary",
                date = System.currentTimeMillis()
            )
        )

        // 3. Add food expense of ₹500
        val foodId = repository.insertTransaction(
            TransactionEntity(
                type = "expense",
                amount = 500.0,
                title = "Dinner",
                category = "Food",
                date = System.currentTimeMillis()
            )
        )

        // 4. Add transport expense of ₹200
        val transportId = repository.insertTransaction(
            TransactionEntity(
                type = "expense",
                amount = 200.0,
                title = "Metro",
                category = "Transport",
                date = System.currentTimeMillis()
            )
        )

        // 5. Add savings of ₹5,000
        val savingsId = repository.insertTransaction(
            TransactionEntity(
                type = "saving",
                amount = 5000.0,
                title = "Emergency Fund",
                category = "Emergency Fund",
                date = System.currentTimeMillis()
            )
        )

        // 6. Add investment of ₹2,000
        val investmentId = repository.insertTransaction(
            TransactionEntity(
                type = "investment",
                amount = 2000.0,
                title = "SBI Mutual Fund",
                category = "Mutual Funds",
                date = System.currentTimeMillis()
            )
        )

        // 7. Verify dashboard calculations
        var list = repository.allTransactions.first()
        assertEquals(5, list.size)

        var income = list.filter { it.type == "income" }.sumOf { it.amount }
        var expenses = list.filter { it.type == "expense" }.sumOf { it.amount }
        var savings = list.filter { it.type == "saving" }.sumOf { it.amount }
        var investments = list.filter { it.type == "investment" }.sumOf { it.amount }
        var balance = income - expenses - savings - investments

        assertEquals(30000.0, income, 0.001)
        assertEquals(700.0, expenses, 0.001)
        assertEquals(5000.0, savings, 0.001)
        assertEquals(2000.0, investments, 0.001)
        assertEquals(22300.0, balance, 0.001)

        // 11. Edit the ₹500 food expense to ₹700
        val foodTx = database.transactionDao().getTransactionById(foodId)
        assertNotNull(foodTx)
        repository.updateTransaction(foodTx!!.copy(amount = 700.0))

        // 12. Verify all totals update
        list = repository.allTransactions.first()
        income = list.filter { it.type == "income" }.sumOf { it.amount }
        expenses = list.filter { it.type == "expense" }.sumOf { it.amount }
        balance = income - expenses - savings - investments

        assertEquals(900.0, expenses, 0.001)
        assertEquals(22100.0, balance, 0.001)

        // 13. Delete the transport transaction
        repository.deleteTransactionById(transportId)

        // 14. Verify totals update again
        list = repository.allTransactions.first()
        assertEquals(4, list.size)
        expenses = list.filter { it.type == "expense" }.sumOf { it.amount }
        balance = income - expenses - savings - investments

        assertEquals(700.0, expenses, 0.001)
        assertEquals(22300.0, balance, 0.001)

        // 15. Search for the food transaction
        val searchFood = list.filter { it.title.contains("Dinner", ignoreCase = true) || it.category.contains("Food", ignoreCase = true) }
        assertEquals(1, searchFood.size)
        assertEquals(700.0, searchFood.first().amount, 0.001)

        // 16. Filter by expenses
        val expenseFilter = list.filter { it.type == "expense" }
        assertEquals(1, expenseFilter.size)
        assertEquals("Dinner", expenseFilter.first().title)

        // 17. Savings Rate calculation: (Savings / Income) * 100
        val summary = FinancialSummary(totalIncome = income, totalExpenses = expenses, totalSavings = savings, totalInvestments = investments)
        assertEquals(22300.0, summary.currentBalance, 0.001)
        val expectedSavingsRate = (5000.0 / 30000.0) * 100.0
        assertEquals(expectedSavingsRate, summary.savingsRate, 0.01)
    }
}
