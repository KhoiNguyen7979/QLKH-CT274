package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.StockDanger

/**
 * Component cảnh báo tồn kho thấp.
 * Hiển thị: icon warning + "Sản phẩm {name} ({code}) sắp hết hàng" + số lượng.
 * Màu nền đỏ mờ (StockDanger 8% alpha).
 * Click → navigate đến chi tiết sản phẩm.
 * Dùng trong DashboardScreen.
 */
@Composable
fun LowStockWarning(
    productName: String,
    productCode: String,
    quantity: Int,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = StockDanger.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon warning
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = StockDanger,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Thông báo: "Sản phẩm X (Y) sắp hết hàng"
            Text(
                text = stringResource(R.string.low_stock_warning_message, productName, productCode),
                style = MaterialTheme.typography.bodyMedium,
                color = StockDanger,
                modifier = Modifier.weight(1f)
            )
            // Số lượng tồn kho
            Text(
                text = stringResource(R.string.product_qty_unit, quantity),
                style = MaterialTheme.typography.labelLarge,
                color = StockDanger
            )
        }
    }
}
