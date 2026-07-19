package com.example.ui.screens

import com.example.data.Product

const val LOW_STOCK_THRESHOLD = 10
const val HIGH_STOCK_THRESHOLD = 50

enum class StockFilter {
    ALL,
    LOW_STOCK,
    HIGH_STOCK
}

enum class SortCriteria {
    NONE,
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC,
    QTY_ASC,
    QTY_DESC
}

fun applyAdvancedFilters(
    products: List<Product>,
    stockFilter: StockFilter,
    sortCriteria: SortCriteria,
    minPrice: Double?,
    maxPrice: Double?,
    minQuantity: Int?,
    maxQuantity: Int?
): List<Product> {
    val filtered = products.filter { product ->
        val matchesStock = when (stockFilter) {
            StockFilter.ALL -> true
            StockFilter.LOW_STOCK -> product.quantity <= LOW_STOCK_THRESHOLD
            StockFilter.HIGH_STOCK -> product.quantity >= HIGH_STOCK_THRESHOLD
        }

        val matchesMinPrice = minPrice == null || product.price >= minPrice
        val matchesMaxPrice = maxPrice == null || product.price <= maxPrice
        val matchesMinQuantity = minQuantity == null || product.quantity >= minQuantity
        val matchesMaxQuantity = maxQuantity == null || product.quantity <= maxQuantity

        matchesStock && matchesMinPrice && matchesMaxPrice && matchesMinQuantity && matchesMaxQuantity
    }

    return when (sortCriteria) {
        SortCriteria.NONE -> filtered
        SortCriteria.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
        SortCriteria.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
        SortCriteria.PRICE_ASC -> filtered.sortedBy { it.price }
        SortCriteria.PRICE_DESC -> filtered.sortedByDescending { it.price }
        SortCriteria.QTY_ASC -> filtered.sortedBy { it.quantity }
        SortCriteria.QTY_DESC -> filtered.sortedByDescending { it.quantity }
    }
}

fun toggleSortCriteria(current: SortCriteria, selected: SortCriteria): SortCriteria {
    return if (current == selected) SortCriteria.NONE else selected
}

fun validateFilterRanges(
    minPrice: Double?,
    maxPrice: Double?,
    minQuantity: Int?,
    maxQuantity: Int?
): String? {
    if (minPrice != null && minPrice < 0) return "Giá tối thiểu không được nhỏ hơn 0."
    if (maxPrice != null && maxPrice < 0) return "Giá tối đa không được nhỏ hơn 0."
    if (minPrice != null && maxPrice != null && minPrice > maxPrice) return "Giá từ không được lớn hơn giá đến."
    if (minQuantity != null && minQuantity < 0) return "Số lượng tối thiểu không hợp lệ."
    if (maxQuantity != null && maxQuantity < 0) return "Số lượng tối đa không hợp lệ."
    if (minQuantity != null && maxQuantity != null && minQuantity > maxQuantity) return "Số lượng từ không được lớn hơn số lượng đến."
    return null
}

fun sanitizeDecimalInput(value: String): String {
    val normalized = value.replace(",", ".").filter { it.isDigit() || it == '.' }
    val firstDotIndex = normalized.indexOf('.')
    return if (firstDotIndex == -1) {
        normalized
    } else {
        normalized.substring(0, firstDotIndex + 1) +
                normalized.substring(firstDotIndex + 1).replace(".", "")
    }
}

fun formatNumberInput(value: Double): String {
    return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}
