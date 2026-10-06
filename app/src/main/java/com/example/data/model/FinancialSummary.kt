package com.example.data.model

data class FinancialSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalSavings: Double = 0.0,
    val totalInvestments: Double = 0.0
) {
    val currentBalance: Double
        get() = totalIncome - totalExpenses - totalSavings - totalInvestments

    val savingsRate: Double
        get() = if (totalIncome > 0.0) {
            ((totalSavings / totalIncome) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0

    val allocationRate: Double
        get() = if (totalIncome > 0.0) {
            (((totalSavings + totalInvestments) / totalIncome) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0
}

enum class SortOrder(val label: String) {
    NEWEST_FIRST("Newest first"),
    OLDEST_FIRST("Oldest first"),
    HIGHEST_AMOUNT("Highest amount"),
    LOWEST_AMOUNT("Lowest amount")
}

enum class DateRangeFilter(val label: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_YEAR("This Year")
}
