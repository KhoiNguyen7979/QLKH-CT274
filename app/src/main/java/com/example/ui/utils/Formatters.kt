package com.example.ui.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Định dạng số tiền theo VND.
 * Ví dụ: 250000 → "250.000 ₫"
 */
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    return format.format(amount)
}

/**
 * Định dạng rút gọn số tiền lớn.
 * - ≥ 1 tỷ: "X.X tỷ"
 * - ≥ 1 triệu: "X.X tr"
 * - ≥ 1 nghìn: "X.X n"
 * - < 1 nghìn: hiển thị nguyên
 */
fun formatCompactCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000_000 -> String.format(Locale.US, "%.1f tỷ", amount / 1_000_000_000.0)
        amount >= 1_000_000 -> String.format(Locale.US, "%.1f tr", amount / 1_000_000.0)
        amount >= 1_000 -> String.format(Locale.US, "%.1f n", amount / 1_000.0)
        else -> String.format(Locale.US, "%.0f", amount)
    }
}

/**
 * Định dạng rút gọn số lượng lớn.
 * - ≥ 1 triệu: "X.XM"
 * - ≥ 1 nghìn: "X.Xn"
 * - < 1 nghìn: hiển thị nguyên
 */
fun formatCompactNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.1fn", number / 1_000.0)
        else -> number.toString()
    }
}
