package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.DefaultPaymentMethods
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIcon
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InvestmentPurple
import com.example.ui.theme.SavingBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    existingTransaction: TransactionEntity? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    currency: Currency,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Double, title: String, category: String, date: Long, paymentMethod: String, description: String) -> Unit,
    onAddCustomCategory: (name: String, type: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var selectedType by remember {
        mutableStateOf(
            existingTransaction?.let { TransactionType.fromId(it.type) } ?: initialType
        )
    }

    var amountText by remember {
        mutableStateOf(existingTransaction?.let {
            if (it.amount % 1.0 == 0.0) it.amount.toLong().toString() else it.amount.toString()
        } ?: "")
    }

    var title by remember { mutableStateOf(existingTransaction?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(existingTransaction?.category ?: "") }
    var selectedDate by remember { mutableLongStateOf(existingTransaction?.date ?: System.currentTimeMillis()) }
    var selectedPaymentMethod by remember { mutableStateOf(existingTransaction?.paymentMethod ?: "Cash") }
    var description by remember { mutableStateOf(existingTransaction?.description ?: "") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Filter categories based on selected transaction type
    val relevantCategories = categories.filter {
        it.type.equals(selectedType.id, ignoreCase = true)
    }

    // Default category selection if not set or invalid for selected type
    LaunchedEffect(selectedType, relevantCategories) {
        if (relevantCategories.isNotEmpty() && relevantCategories.none { it.name.equals(selectedCategory, ignoreCase = true) }) {
            selectedCategory = relevantCategories.first().name
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTransaction == null) "New Transaction" else "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate600)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Selector (4 Types: Income, Expense, Saving, Investment)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate200.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    val (activeBg, activeFg) = when (type) {
                        TransactionType.INCOME -> Pair(IncomeGreen, Color.White)
                        TransactionType.EXPENSE -> Pair(ExpenseRed, Color.White)
                        TransactionType.SAVING -> Pair(SavingBlue, Color.White)
                        TransactionType.INVESTMENT -> Pair(InvestmentPurple, Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) activeBg else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) activeFg else Slate600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
                        amountText = input
                        validationError = null
                    }
                },
                label = { Text("Amount (${currency.symbol})") },
                prefix = {
                    Text(
                        text = "${currency.symbol} ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Preset Amount Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                listOf(100.0, 500.0, 1000.0, 5000.0).forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Slate200.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable {
                                val current = amountText.toDoubleOrNull() ?: 0.0
                                val next = current + preset
                                amountText = if (next % 1.0 == 0.0) next.toLong().toString() else next.toString()
                            }
                    ) {
                        Text(
                            text = "+${currency.symbol}${preset.toLong()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Slate800,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = { Text("Title / Payee") },
                placeholder = { Text("e.g., Grocery Shopping, Salary, Rent") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Slate800
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                relevantCategories.forEach { cat ->
                    val isCatSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isCatSelected) MaterialTheme.colorScheme.primary else Slate200.copy(alpha = 0.5f),
                        contentColor = if (isCatSelected) Color.White else Slate800,
                        modifier = Modifier
                            .clickable { selectedCategory = cat.name }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // Add Custom Category button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate400),
                    modifier = Modifier.clickable { showAddCategoryDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add custom category",
                            modifier = Modifier.size(16.dp),
                            tint = Slate600
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Row
            Text(
                text = "Date",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Slate800
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Slate200.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selected = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }
                                selectedDate = selected.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateFormat.format(Date(selectedDate)),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Slate900
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pick date",
                        tint = Slate600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method Selection
            Text(
                text = "Payment Method",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Slate800
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DefaultPaymentMethods.list.forEach { method ->
                    val isMethodSelected = selectedPaymentMethod == method
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isMethodSelected) Slate900 else Slate200.copy(alpha = 0.5f),
                        contentColor = if (isMethodSelected) Color.White else Slate800,
                        modifier = Modifier.clickable { selectedPaymentMethod = method }
                    ) {
                        Text(
                            text = method,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isMethodSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes / Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Add any extra notes or memo") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Validation error message if any
            if (validationError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = validationError ?: "",
                    color = ExpenseRed,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Transaction Button
            Button(
                onClick = {
                    val amountVal = amountText.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0.0) {
                        validationError = "Please enter a valid amount greater than 0."
                        return@Button
                    }
                    if (title.isBlank()) {
                        validationError = "Please enter a title."
                        return@Button
                    }
                    val cat = if (selectedCategory.isBlank()) {
                        relevantCategories.firstOrNull()?.name ?: "Other"
                    } else selectedCategory

                    onSave(
                        selectedType.id,
                        amountVal,
                        title,
                        cat,
                        selectedDate,
                        selectedPaymentMethod,
                        description
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                Text(
                    text = if (existingTransaction == null) "Save Transaction" else "Update Transaction",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }

    // Inline Dialog to Add Custom Category
    if (showAddCategoryDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("New ${selectedType.displayName} Category") },
            text = {
                Column {
                    Text(
                        text = "Enter a name for your custom category:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onAddCustomCategory(newCategoryName.trim(), selectedType.id)
                            selectedCategory = newCategoryName.trim()
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text("Add", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
