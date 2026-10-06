package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.FinancialSummary
import com.example.ui.components.ChartSlice
import com.example.ui.components.ExpenseDonutChart
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.MonthlyBarData
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenLight
import com.example.ui.theme.InvestmentPurple
import com.example.ui.theme.InvestmentPurpleLight
import com.example.ui.theme.SavingBlue
import com.example.ui.theme.SavingBlueLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

enum class AnalyticsTimeframe(val label: String) {
    ALL_TIME("All Time"),
    THIS_YEAR("This Year"),
    THIS_MONTH("This Month")
}

@Composable
fun AnalyticsScreen(
    allTransactions: List<TransactionEntity>,
    currency: Currency,
    donutSlices: List<ChartSlice>,
    monthlyBarData: List<MonthlyBarData>,
    modifier: Modifier = Modifier
) {
    var timeframe by remember { mutableStateOf(AnalyticsTimeframe.ALL_TIME) }

    // Filter transactions based on selected timeframe
    val filteredTransactions = remember(allTransactions, timeframe) {
        val now = java.util.Calendar.getInstance()
        when (timeframe) {
            AnalyticsTimeframe.ALL_TIME -> allTransactions
            AnalyticsTimeframe.THIS_YEAR -> {
                val currentYear = now.get(java.util.Calendar.YEAR)
                allTransactions.filter {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.date }
                    cal.get(java.util.Calendar.YEAR) == currentYear
                }
            }
            AnalyticsTimeframe.THIS_MONTH -> {
                val currentYear = now.get(java.util.Calendar.YEAR)
                val currentMonth = now.get(java.util.Calendar.MONTH)
                allTransactions.filter {
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = it.date }
                    cal.get(java.util.Calendar.YEAR) == currentYear && cal.get(java.util.Calendar.MONTH) == currentMonth
                }
            }
        }
    }

    // Calculations
    val incomeTxs = filteredTransactions.filter { it.type.equals("income", ignoreCase = true) }
    val expenseTxs = filteredTransactions.filter { it.type.equals("expense", ignoreCase = true) }
    val savingTxs = filteredTransactions.filter { it.type.equals("saving", ignoreCase = true) }
    val investmentTxs = filteredTransactions.filter { it.type.equals("investment", ignoreCase = true) }

    val totalIncome = incomeTxs.sumOf { it.amount }
    val totalExpense = expenseTxs.sumOf { it.amount }
    val totalSavings = savingTxs.sumOf { it.amount }
    val totalInvestments = investmentTxs.sumOf { it.amount }

    // Spending Insights calculations
    val highestCategoryEntry = expenseTxs.groupBy { it.category }
        .mapValues { it.value.sumOf { tx -> tx.amount } }
        .maxByOrNull { it.value }

    val highestExpenseTx = expenseTxs.maxByOrNull { it.amount }

    val monthsCount = 6.0
    val avgMonthlyIncome = if (totalIncome > 0) totalIncome / monthsCount else 0.0
    val avgMonthlyExpense = if (totalExpense > 0) totalExpense / monthsCount else 0.0

    // Safe rates
    val savingsRate = if (totalIncome > 0.0) ((totalSavings / totalIncome) * 100.0).coerceIn(0.0, 100.0) else 0.0
    val allocationRate = if (totalIncome > 0.0) (((totalSavings + totalInvestments) / totalIncome) * 100.0).coerceIn(0.0, 100.0) else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Slate900
                )

                // Timeframe selector
                Row(
                    modifier = Modifier
                        .background(Slate100, RoundedCornerShape(12.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnalyticsTimeframe.entries.forEach { tf ->
                        val isSelected = timeframe == tf
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Slate900 else Color.Transparent,
                            contentColor = if (isSelected) Color.White else Slate600,
                            modifier = Modifier.clickable { timeframe = tf }
                        ) {
                            Text(
                                text = tf.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Expense Breakdown Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ExpenseRedLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Expense Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExpenseDonutChart(
                        slices = donutSlices,
                        currency = currency
                    )
                }
            }
        }

        // Section: Monthly Income vs Expenses (Bar Chart)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(IncomeGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Monthly Income vs Expenses",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    IncomeExpenseBarChart(
                        data = monthlyBarData,
                        currency = currency
                    )
                }
            }
        }

        // Section: Savings & Investment Allocation Rate
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SavingBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = SavingBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Savings & Investment Rate",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Savings Rate Progress
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Savings Rate (Savings / Income)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate600
                            )
                            Text(
                                text = "%.1f%%".format(savingsRate),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SavingBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (savingsRate / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SavingBlue,
                            trackColor = SavingBlueLight
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Total Allocation Rate Progress (Savings + Investments)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Wealth Allocation (Savings + Invest / Income)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate600
                            )
                            Text(
                                text = "%.1f%%".format(allocationRate),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = InvestmentPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (allocationRate / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = InvestmentPurple,
                            trackColor = InvestmentPurpleLight
                        )
                    }
                }
            }
        }

        // Section: Spending Insights Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Spending Insights",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )

                // Highest spending category
                InsightItemCard(
                    title = "Highest Spending Category",
                    value = highestCategoryEntry?.key ?: "None recorded",
                    subValue = highestCategoryEntry?.let { currency.format(it.value) } ?: "-",
                    icon = Icons.Default.TrendingUp,
                    tint = ExpenseRed
                )

                // Highest single expense
                InsightItemCard(
                    title = "Highest Single Expense",
                    value = highestExpenseTx?.title ?: "None recorded",
                    subValue = highestExpenseTx?.let { currency.format(it.amount) } ?: "-",
                    icon = Icons.Default.ArrowDownward,
                    tint = ExpenseRed
                )

                // Average Monthly Expenses
                InsightItemCard(
                    title = "Average Monthly Expense",
                    value = currency.format(avgMonthlyExpense),
                    subValue = "Calculated across past periods",
                    icon = Icons.Default.ArrowDownward,
                    tint = Slate700
                )

                // Average Monthly Income
                InsightItemCard(
                    title = "Average Monthly Income",
                    value = currency.format(avgMonthlyIncome),
                    subValue = "Calculated across past periods",
                    icon = Icons.Default.ArrowUpward,
                    tint = IncomeGreen
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun InsightItemCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    tint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Slate900
                )
            }

            Text(
                text = subValue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = tint
            )
        }
    }
}
