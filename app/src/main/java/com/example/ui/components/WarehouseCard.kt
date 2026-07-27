package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Card wrapper tùy chỉnh.
 * - shape: medium (12dp corners)
 * - elevation: 1dp
 * - Optional onClick: nếu có thì card có thể click được
 * Dùng để bọc nội dung trong các màn hình khác nhau.
 */
@Composable
fun WarehouseCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,  // Optional: card có thể click được
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = content
    )
}
