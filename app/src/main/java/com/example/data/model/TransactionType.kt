package com.example.data.model

enum class TransactionType(val id: String, val displayName: String) {
    INCOME("income", "Income"),
    EXPENSE("expense", "Expense"),
    SAVING("saving", "Saving"),
    INVESTMENT("investment", "Investment");

    companion object {
        fun fromId(id: String): TransactionType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: EXPENSE
        }
    }
}

object DefaultPaymentMethods {
    val list = listOf(
        "Cash",
        "UPI",
        "Debit Card",
        "Credit Card",
        "Bank Transfer",
        "Other"
    )
}
