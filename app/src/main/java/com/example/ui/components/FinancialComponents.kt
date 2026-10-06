package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.model.Currency
import com.example.data.model.TransactionType
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
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CategoryIcon(
    categoryName: String,
    modifier: Modifier = Modifier,
    type: String = "expense"
) {
    val (icon, tint, bg) = getCategoryVisuals(categoryName, type)
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = categoryName,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

fun getCategoryVisuals(categoryName: String, type: String): Triple<ImageVector, Color, Color> {
    val lower = categoryName.lowercase()
    val icon = when {
        lower.contains("salary") -> Icons.Default.Payments
        lower.contains("freelance") || lower.contains("work") -> Icons.Default.Work
        lower.contains("business") -> Icons.Default.Storefront
        lower.contains("interest") || lower.contains("trend") -> Icons.Default.TrendingUp
        lower.contains("gift") -> Icons.Default.CardGiftcard
        lower.contains("food") || lower.contains("grocer") || lower.contains("dine") -> Icons.Default.Restaurant
        lower.contains("transport") || lower.contains("fuel") || lower.contains("cab") -> Icons.Default.DirectionsCar
        lower.contains("shopping") || lower.contains("cloth") -> Icons.Default.ShoppingBag
        lower.contains("bill") || lower.contains("util") || lower.contains("electric") -> Icons.Default.ReceiptLong
        lower.contains("rent") || lower.contains("house") -> Icons.Default.Home
        lower.contains("movie") || lower.contains("entertain") -> Icons.Default.Movie
        lower.contains("health") || lower.contains("medic") || lower.contains("doctor") -> Icons.Default.LocalHospital
        lower.contains("education") || lower.contains("school") || lower.contains("course") -> Icons.Default.School
        lower.contains("subscription") -> Icons.Default.Subscriptions
        lower.contains("travel") || lower.contains("flight") -> Icons.Default.Flight
        lower.contains("emergency") -> Icons.Default.Shield
        lower.contains("saving") -> Icons.Default.Savings
        lower.contains("mutual") -> Icons.Default.PieChart
        lower.contains("stock") -> Icons.Default.ShowChart
        lower.contains("deposit") -> Icons.Default.LockClock
        lower.contains("gold") -> Icons.Default.MonetizationOn
        lower.contains("bond") -> Icons.Default.Description
        else -> when (type.lowercase()) {
            "income" -> Icons.Default.AccountBalanceWallet
            "saving" -> Icons.Default.Savings
            "investment" -> Icons.Default.TrendingUp
            else -> Icons.Default.Category
        }
    }

    val (tint, bg) = when (type.lowercase()) {
        "income" -> Pair(IncomeGreen, IncomeGreenLight)
        "saving" -> Pair(SavingBlue, SavingBlueLight)
        "investment" -> Pair(InvestmentPurple, InvestmentPurpleLight)
        else -> Pair(ExpenseRed, ExpenseRedLight)
    }

    return Triple(icon, tint, bg)
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(transaction.date))
    val txType = TransactionType.fromId(transaction.type)

    val (sign, amountColor) = when (txType) {
        TransactionType.INCOME -> Pair("+", IncomeGreen)
        TransactionType.EXPENSE -> Pair("-", ExpenseRed)
        TransactionType.SAVING -> Pair("", SavingBlue)
        TransactionType.INVESTMENT -> Pair("", InvestmentPurple)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}")
            .clickable { onClick() },
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
            CategoryIcon(
                categoryName = transaction.category,
                type = transaction.type
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    Text(text = "•", style = MaterialTheme.typography.bodySmall, color = Slate400)
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    if (transaction.paymentMethod.isNotEmpty()) {
                        Text(text = "•", style = MaterialTheme.typography.bodySmall, color = Slate400)
                        Text(
                            text = transaction.paymentMethod,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Slate600,
                            modifier = Modifier
                                .background(Slate100, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$sign${currency.format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                )
                Text(
                    text = txType.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Slate500
                )
            }
        }
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    amount: Double,
    currency: Currency,
    color: Color,
    bgColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Slate600
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currency.format(amount),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class ChartSlice(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun ExpenseDonutChart(
    slices: List<ChartSlice>,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value }
    if (total <= 0.0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expenses recorded for this period",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                var startAngle = -90f
                val strokeWidth = 32.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                slices.forEach { slice ->
                    val sweepAngle = ((slice.value / total) * 360f).toFloat()
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate500
                )
                Text(
                    text = currency.format(total),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            slices.forEach { slice ->
                val percentage = ((slice.value / total) * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(slice.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = slice.label,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = Slate700
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = currency.format(slice.value),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Slate900
                        )
                    }
                }
            }
        }
    }
}

data class MonthlyBarData(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

@Composable
fun IncomeExpenseBarChart(
    data: List<MonthlyBarData>,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty() || data.all { it.income == 0.0 && it.expense == 0.0 }) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No monthly data to display yet",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400
            )
        }
        return
    }

    val maxVal = data.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(100.0) ?: 100.0

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(IncomeGreen))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Income", style = MaterialTheme.typography.labelSmall, color = Slate600)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ExpenseRed))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Expense", style = MaterialTheme.typography.labelSmall, color = Slate600)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Income bar
                        val incomeHeightRatio = (item.income / maxVal).coerceIn(0.02, 1.0).toFloat()
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((110 * incomeHeightRatio).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(IncomeGreen)
                        )
                        // Expense bar
                        val expenseHeightRatio = (item.expense / maxVal).coerceIn(0.02, 1.0).toFloat()
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((110 * expenseHeightRatio).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(ExpenseRed)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.monthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String = "Delete Transaction",
    message: String = "Are you sure you want to delete this transaction? This action cannot be undone.",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = ExpenseRed
            )
        },
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text("Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate600)
            }
        }
    )
}
