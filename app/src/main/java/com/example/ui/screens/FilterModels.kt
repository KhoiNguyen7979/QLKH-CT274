package com.example.ui.screens

import com.example.R
import com.example.data.Product

/** Ngưỡng tồn kho thấp: sản phẩm có quantity ≤ 10 được coi là "sắp hết hàng" */
const val LOW_STOCK_THRESHOLD = 10

/** Ngưỡng tồn kho cao: sản phẩm có quantity ≥ 50 được coi là "dồi dào" */
const val HIGH_STOCK_THRESHOLD = 50

/** Enum các bộ lọc trạng thái tồn kho */
enum class StockFilter {
    ALL,        // Tất cả
    LOW_STOCK,  // Tồn kho thấp (≤ 10)
    HIGH_STOCK  // Tồn kho cao (≥ 50)
}

/** Enum các tiêu chí sắp xếp */
enum class SortCriteria {
    NONE,       // Không sắp xếp
    NAME_ASC,   // Tên A-Z
    NAME_DESC,  // Tên Z-A
    PRICE_ASC,  // Giá thấp → cao
    PRICE_DESC, // Giá cao → thấp
    QTY_ASC,    // Số lượng ít → nhiều
    QTY_DESC    // Số lượng nhiều → ít
}

/**
 * Áp dụng bộ lọc nâng cao cho danh sách sản phẩm.
 * Kết hợp: trạng thái tồn kho + khoảng giá + khoảng số lượng + sắp xếp.
 * @return Danh sách sản phẩm đã được lọc và sắp xếp.
 */
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
        // Kiểm tra trạng thái tồn kho
        val matchesStock = when (stockFilter) {
            StockFilter.ALL -> true
            StockFilter.LOW_STOCK -> product.quantity <= LOW_STOCK_THRESHOLD
            StockFilter.HIGH_STOCK -> product.quantity >= HIGH_STOCK_THRESHOLD
        }

        // Kiểm tra khoảng giá
        val matchesMinPrice = minPrice == null || product.price >= minPrice
        val matchesMaxPrice = maxPrice == null || product.price <= maxPrice
        // Kiểm tra khoảng số lượng
        val matchesMinQuantity = minQuantity == null || product.quantity >= minQuantity
        val matchesMaxQuantity = maxQuantity == null || product.quantity <= maxQuantity

        matchesStock && matchesMinPrice && matchesMaxPrice && matchesMinQuantity && matchesMaxQuantity
    }

    // Sắp xếp kết quả
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

/**
 * Toggle tiêu chí sắp xếp: nếu đang chọn thì bỏ, nếu chưa chọn thì chọn.
 * @return SortCriteria.NONE nếu bỏ chọn, ngược lại trả về selected.
 */
fun toggleSortCriteria(current: SortCriteria, selected: SortCriteria): SortCriteria {
    return if (current == selected) SortCriteria.NONE else selected
}

/**
 * Kiểm tra tính hợp lệ của các khoảng giá/số lượng.
 * @return null nếu hợp lệ, hoặc resource ID của thông báo lỗi.
 */
fun validateFilterRanges(
    minPrice: Double?,
    maxPrice: Double?,
    minQuantity: Int?,
    maxQuantity: Int?
): Int? {
    if (minPrice != null && minPrice < 0) return R.string.error_price_min_negative
    if (maxPrice != null && maxPrice < 0) return R.string.error_price_max_negative
    if (minPrice != null && maxPrice != null && minPrice > maxPrice) return R.string.error_price_range_invalid
    if (minQuantity != null && minQuantity < 0) return R.string.error_qty_min_negative
    if (maxQuantity != null && maxQuantity < 0) return R.string.error_qty_max_negative
    if (minQuantity != null && maxQuantity != null && minQuantity > maxQuantity) return R.string.error_qty_range_invalid
    return null
}

/**
 * Chuẩn hóa input số thập phân: thay dấu phẩy bằng dấu chấm, chỉ giữ digit và dấu chấm.
 * Xử lý trường hợp nhập nhiều dấu chấm.
 */
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

/**
 * Định dạng số cho input: nếu là số nguyên thì bỏ phần thập phân.
 * Ví dụ: 100.0 → "100", 100.5 → "100.5"
 */
fun formatNumberInput(value: Double): String {
    return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
}
