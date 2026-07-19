package com.example.ui.utils

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    return format.format(amount)
}

fun formatCompactCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000_000 -> String.format(Locale.US, "%.1f tỷ", amount / 1_000_000_000.0)
        amount >= 1_000_000 -> String.format(Locale.US, "%.1f tr", amount / 1_000_000.0)
        amount >= 1_000 -> String.format(Locale.US, "%.1f n", amount / 1_000.0)
        else -> String.format(Locale.US, "%.0f", amount)
    }
}

fun formatCompactNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.1fn", number / 1_000.0)
        else -> number.toString()
    }
}
