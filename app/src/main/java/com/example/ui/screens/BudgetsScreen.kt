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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.ui.components.CategoryIcon
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Calendar

@Composable
fun BudgetsScreen(
    budgets: List<BudgetEntity>,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    currency: Currency,
    onSetBudget: (category: String, limit: Double) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSetDialog by remember { mutableStateOf(false) }
    var budgetToDelete by remember { mutableStateOf<BudgetEntity?>(null) }

    // Filter current month expenses
    val currentMonthExpenses = remember(transactions) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        transactions.filter { tx ->
            if (!tx.type.equals("expense", ignoreCase = true)) return@filter false
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
            txCal.get(Calendar.YEAR) == year && txCal.get(Calendar.MONTH) == month
        }
    }

    val overallBudget = budgets.firstOrNull { it.category == BudgetEntity.OVERALL_CATEGORY }
    val categoryBudgets = budgets.filter { it.category != BudgetEntity.OVERALL_CATEGORY }

    val totalSpentThisMonth = currentMonthExpenses.sumOf { it.amount }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Budgets",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Slate900
                )
            }

            // Overall Monthly Spending Budget Card
            item {
                val overallLimit = overallBudget?.monthlyLimit ?: 0.0
                val progress = if (overallLimit > 0) (totalSpentThisMonth / overallLimit).toFloat() else 0f
                val isExceeded = overallLimit > 0 && totalSpentThisMonth > overallLimit
                val remaining = overallLimit - totalSpentThisMonth

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OVERALL MONTHLY BUDGET",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Slate400
                            )
                            if (overallBudget != null) {
                                IconButton(
                                    onClick = { budgetToDelete = overallBudget },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Overall Budget",
                                        tint = Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (overallLimit > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = currency.format(totalSpentThisMonth),
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isExceeded) ExpenseRed else Color.White
                                    )
                                    Text(
                                        text = "Spent of ${currency.format(overallLimit)} limit",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate400
                                    )
                                }

                                Text(
                                    text = if (isExceeded) "Exceeded!" else "${currency.format(remaining)} left",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isExceeded) ExpenseRed else IncomeGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (isExceeded) ExpenseRed else if (progress > 0.8f) GoldAccent else IncomeGreen,
                                trackColor = Slate700
                            )

                            if (isExceeded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(ExpenseRedLight, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Exceeded monthly limit by ${currency.format(totalSpentThisMonth - overallLimit)}!",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = ExpenseRed
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No overall monthly budget set",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Slate400
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showSetDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                            ) {
                                Text("Set Monthly Budget")
                            }
                        }
                    }
                }
            }

            // Category Budgets Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Budgets",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                }
            }

            if (categoryBudgets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
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
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Slate500,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No category budgets configured",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate800
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Set limits for specific categories like Food (₹5,000) or Transport (₹3,000).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate500,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(categoryBudgets, key = { it.id }) { budget ->
                    val spentOnCategory = currentMonthExpenses
                        .filter { it.category.equals(budget.category, ignoreCase = true) }
                        .sumOf { it.amount }
                    val progress = if (budget.monthlyLimit > 0) (spentOnCategory / budget.monthlyLimit).toFloat() else 0f
                    val percentage = (progress * 100).toInt()
                    val isExceeded = spentOnCategory > budget.monthlyLimit
                    val remaining = budget.monthlyLimit - spentOnCategory

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryIcon(categoryName = budget.category, type = "expense")
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = budget.category,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Slate900
                                        )
                                        Text(
                                            text = "Budget: ${currency.format(budget.monthlyLimit)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate500
                                        )
                                    }
                                }

                                IconButton(onClick = { budgetToDelete = budget }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Budget",
                                        tint = Slate400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isExceeded) ExpenseRed else if (progress > 0.8f) GoldAccent else IncomeGreen,
                                trackColor = Slate100
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Spent: ${currency.format(spentOnCategory)} ($percentage%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isExceeded) ExpenseRed else Slate700
                                    )
                                )
                                Text(
                                    text = if (isExceeded) "Exceeded by ${currency.format(spentOnCategory - budget.monthlyLimit)}" else "Remaining: ${currency.format(remaining)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExceeded) ExpenseRed else IncomeGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB to set new budget
        FloatingActionButton(
            onClick = { showSetDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("set_budget_fab"),
            shape = CircleShape,
            containerColor = Slate900,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Set Budget",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Set Budget Dialog
    if (showSetDialog) {
        SetBudgetDialog(
            currency = currency,
            expenseCategories = categories.filter { it.type.equals("expense", ignoreCase = true) },
            onDismiss = { showSetDialog = false },
            onConfirm = { category, limit ->
                onSetBudget(category, limit)
                showSetDialog = false
            }
        )
    }

    // Confirmation dialog for deletion
    budgetToDelete?.let { budget ->
        val catName = if (budget.category == BudgetEntity.OVERALL_CATEGORY) "Overall Monthly Budget" else budget.category
        ConfirmDeleteDialog(
            title = "Delete Budget",
            message = "Are you sure you want to delete the budget for '$catName'?",
            onConfirm = {
                onDeleteBudget(budget.id)
                budgetToDelete = null
            },
            onDismiss = { budgetToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetDialog(
    currency: Currency,
    expenseCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (category: String, limit: Double) -> Unit
) {
    val options = listOf("Overall Monthly Budget") + expenseCategories.map { it.name }
    var selectedOption by remember { mutableStateOf(options.first()) }
    var expanded by remember { mutableStateOf(false) }
    var limitText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select whether this is an overall monthly budget or category-specific.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedOption,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Budget Type / Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    selectedOption = opt
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Monthly Limit (${currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(text = error ?: "", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limitVal = limitText.toDoubleOrNull() ?: 0.0
                    if (limitVal <= 0.0) {
                        error = "Limit must be greater than zero."
                        return@Button
                    }
                    val targetCategory = if (selectedOption == "Overall Monthly Budget") {
                        BudgetEntity.OVERALL_CATEGORY
                    } else selectedOption

                    onConfirm(targetCategory, limitVal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate600)
            }
        }
    )
}
