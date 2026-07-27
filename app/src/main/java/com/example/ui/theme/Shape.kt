package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shapes tùy chỉnh cho ứng dụng.
 * Tất cả đều là RoundedCornerShape với các mức:
 * - small: 8.dp (card nhỏ, button)
 * - medium: 12.dp (card lớn, dialog)
 * - large: 16.dp (bottom sheet)
 * - extraLarge: 24.dp (modal)
 */
val WarehouseShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
