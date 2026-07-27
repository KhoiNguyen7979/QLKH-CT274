package com.example.ui.utils

import com.example.data.Product
import com.example.ui.screens.LOW_STOCK_THRESHOLD

/**
 * Extension function cho Product.
 * Kiểm tra sản phẩm có tồn kho thấp không (quantity ≤ 10).
 * Dùng để hiển thị cảnh báo màu đỏ.
 */
fun Product.isLowStock(): Boolean = quantity <= LOW_STOCK_THRESHOLD
