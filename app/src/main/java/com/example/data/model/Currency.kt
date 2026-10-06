package com.example.data.model

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

data class Currency(
    val code: String,
    val symbol: String,
    val name: String,
    val isIndianNumbering: Boolean = false
) {
    fun format(amount: Double, showSign: Boolean = false, isPositive: Boolean = true): String {
        val signStr = if (showSign) {
            if (isPositive) "+" else "-"
        } else ""

        val formattedAmount = if (isIndianNumbering) {
            formatIndianCurrency(amount)
        } else {
            val format = NumberFormat.getNumberInstance(Locale.US)
            format.minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            format.maximumFractionDigits = 2
            format.format(amount)
        }

        return "$signStr$symbol$formattedAmount"
    }

    companion object {
        val INR = Currency("INR", "₹", "Indian Rupee (₹)", isIndianNumbering = true)
        val USD = Currency("USD", "$", "US Dollar ($)")
        val EUR = Currency("EUR", "€", "Euro (€)")
        val GBP = Currency("GBP", "£", "British Pound (£)")
        val JPY = Currency("JPY", "¥", "Japanese Yen (¥)")
        val CAD = Currency("CAD", "CA$", "Canadian Dollar (CA$)")
        val AUD = Currency("AUD", "AU$", "Australian Dollar (AU$)")
        val AED = Currency("AED", "AED ", "UAE Dirham (AED)")
        val SGD = Currency("SGD", "S$", "Singapore Dollar (S$)")

        val supportedCurrencies = listOf(INR, USD, EUR, GBP, JPY, CAD, AUD, AED, SGD)

        fun findByCode(code: String): Currency {
            return supportedCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: INR
        }

        private fun formatIndianCurrency(amount: Double): String {
            val wholePart = amount.toLong()
            val fractionalPart = amount - wholePart
            val wholeStr = wholePart.toString()

            val formattedWhole = if (wholeStr.length <= 3) {
                wholeStr
            } else {
                val lastThree = wholeStr.takeLast(3)
                val remaining = wholeStr.dropLast(3)
                val formattedRemaining = buildString {
                    for (i in remaining.indices) {
                        append(remaining[i])
                        val charsFromRight = remaining.length - 1 - i
                        if (charsFromRight > 0 && charsFromRight % 2 == 0) {
                            append(",")
                        }
                    }
                }
                "$formattedRemaining,$lastThree"
            }

            return if (fractionalPart > 0.009) {
                val df = DecimalFormat("#.00")
                val fracStr = df.format(fractionalPart).removePrefix("0")
                "$formattedWhole$fracStr"
            } else {
                formattedWhole
            }
        }
    }
}
