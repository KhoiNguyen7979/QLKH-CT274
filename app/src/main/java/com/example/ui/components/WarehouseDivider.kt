package com.example.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Đường kẻ ngang tùy chỉnh.
 * - Màu: outlineVariant 50% alpha (mờ)
 * - Độ dày: 0.5dp (mỏng)
 * Dùng để phân tách các phần trong card.
 */
@Composable
fun WarehouseDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}
