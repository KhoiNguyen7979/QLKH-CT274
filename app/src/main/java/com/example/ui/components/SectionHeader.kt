package com.example.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

/**
 * Tiêu đề section tùy chỉnh.
 * - style: labelLarge, bold
 * - letterSpacing: 1sp
 * - Optional color override (mặc định: onSurfaceVariant)
 * Dùng để phân tách các section trong màn hình.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        letterSpacing = 1.sp,
        modifier = modifier
    )
}
