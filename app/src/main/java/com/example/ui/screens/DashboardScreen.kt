package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.FinancialSummary
import com.example.data.model.TransactionType
import com.example.ui.components.SummaryStatCard
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenLight
import com.example.ui.theme.InvestmentPurple
import com.example.ui.theme.InvestmentPurpleLight
import com.example.ui.theme.SavingBlue
import com.example.ui.theme.SavingBlueLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun DashboardScreen(
    overallSummary: FinancialSummary,
    currentMonthSummary: FinancialSummary,
    recentTransactions: List<TransactionEntity>,
    currency: Currency,
    onAddTransactionClick: (TransactionType) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // App Bar / Top Identity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MoneyPilot",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Slate900
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen)
                            )
                            Text(
                                text = "Offline & Private",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Slate500
                            )
                        }
                    }

                    // Security / Private Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Slate100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% Local SQLite",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Slate700
                            )
                        }
                    }
                }
            }

            // Hero Balance Card: Prominently showing Total Balance = Income - Expenses - Savings - Investments
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("total_balance_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL BALANCE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Slate400
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate800
                            ) {
                                Text(
                                    text = currency.code,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large formatted balance amount
                        Text(
                            text = currency.format(overallSummary.currentBalance),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.testTag("total_balance_amount")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Financial breakdown pills inside hero card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BalanceMetric(
                                label = "Income",
                                amount = currency.format(overallSummary.totalIncome),
                                color = IncomeGreen
                            )
                            BalanceMetric(
                                label = "Expenses",
                                amount = currency.format(overallSummary.totalExpenses),
                                color = ExpenseRed
                            )
                            BalanceMetric(
                                label = "Savings",
                                amount = currency.format(overallSummary.totalSavings),
                                color = SavingBlue
                            )
                            BalanceMetric(
                                label = "Investments",
                                amount = currency.format(overallSummary.totalInvestments),
                                color = InvestmentPurple
                            )
                        }
                    }
                }
            }

            // Quick Actions: Direct entry for all 4 types
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        label = "Income",
                        icon = Icons.Default.NorthEast,
                        color = IncomeGreen,
                        bgColor = IncomeGreenLight,
                        onClick = { onAddTransactionClick(TransactionType.INCOME) },
                        modifier = Modifier.weight(1f).testTag("quick_add_income")
                    )
                    QuickActionButton(
                        label = "Expense",
                        icon = Icons.Default.SouthWest,
                        color = ExpenseRed,
                        bgColor = ExpenseRedLight,
                        onClick = { onAddTransactionClick(TransactionType.EXPENSE) },
                        modifier = Modifier.weight(1f).testTag("quick_add_expense")
                    )
                    QuickActionButton(
                        label = "Saving",
                        icon = Icons.Default.Savings,
                        color = SavingBlue,
                        bgColor = SavingBlueLight,
                        onClick = { onAddTransactionClick(TransactionType.SAVING) },
                        modifier = Modifier.weight(1f).testTag("quick_add_saving")
                    )
                    QuickActionButton(
                        label = "Invest",
                        icon = Icons.Default.TrendingUp,
                        color = InvestmentPurple,
                        bgColor = InvestmentPurpleLight,
                        onClick = { onAddTransactionClick(TransactionType.INVESTMENT) },
                        modifier = Modifier.weight(1f).testTag("quick_add_investment")
                    )
                }
            }

            // This Month Summary Section
            item {
                Column {
                    Text(
                        text = "This Month",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryStatCard(
                            title = "Monthly Income",
                            amount = currentMonthSummary.totalIncome,
                            currency = currency,
                            color = IncomeGreen,
                            bgColor = IncomeGreenLight,
                            icon = Icons.Default.NorthEast,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCard(
                            title = "Monthly Expenses",
                            amount = currentMonthSummary.totalExpenses,
                            currency = currency,
                            color = ExpenseRed,
                            bgColor = ExpenseRedLight,
                            icon = Icons.Default.SouthWest,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryStatCard(
                            title = "Monthly Savings",
                            amount = currentMonthSummary.totalSavings,
                            currency = currency,
                            color = SavingBlue,
                            bgColor = SavingBlueLight,
                            icon = Icons.Default.Savings,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCard(
                            title = "Monthly Invest",
                            amount = currentMonthSummary.totalInvestments,
                            currency = currency,
                            color = InvestmentPurple,
                            bgColor = InvestmentPurpleLight,
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    if (recentTransactions.isNotEmpty()) {
                        TextButton(
                            onClick = onViewAllTransactionsClick,
                            modifier = Modifier.testTag("view_all_transactions_button")
                        ) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Slate800
                            )
                        }
                    }
                }
            }

            // Recent Transactions List or Empty State
            if (recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("empty_transactions_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Slate100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No transactions yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add your first income or expense to start tracking your money.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate500,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(recentTransactions.take(5)) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        currency = currency,
                        onClick = { onTransactionClick(tx) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar & FAB
            }
        }

        // Floating Action Button (+ button) for quick transaction addition
        FloatingActionButton(
            onClick = { onAddTransactionClick(TransactionType.EXPENSE) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_transaction_fab"),
            shape = CircleShape,
            containerColor = Slate900,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BalanceMetric(
    label: String,
    amount: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate400
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}
