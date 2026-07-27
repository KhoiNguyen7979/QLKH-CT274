package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.Product
import com.example.ui.theme.StockDanger
import com.example.ui.utils.CategoryRegistry
import com.example.ui.utils.formatCompactCurrency
import com.example.ui.utils.formatCurrency
import com.example.ui.utils.isLowStock
import java.io.File

/**
 * Component hiển thị một sản phẩm trong danh sách.
 * Cấu trúc Row:
 * - Thumbnail (ảnh preset hoặc Coil image)
 * - Thông tin (tên, mã, badge danh mục, badge low-stock)
 * - Cột phải (số lượng, giá, tổng GT)
 * Click → navigate đến chi tiết sản phẩm.
 */
@Composable
fun ProductListItem(
    product: Product,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onAdjustStock: (Int) -> Unit
) {
    val meta = CategoryRegistry.getMeta(product.category)

    WarehouseCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ========== THUMBNAIL ==========
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(meta.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrls.isNotEmpty() &&
                    !product.imageUrls.first().startsWith("preset_")
                ) {
                    // Ảnh thật từ file
                    Image(
                        painter = rememberAsyncImagePainter(
                            File(product.imageUrls.first())
                        ),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Ảnh preset → icon danh mục
                    Icon(
                        imageVector = meta.icon,
                        contentDescription = product.category,
                        tint = meta.color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ========== THÔNG TIN SẢN PHẨM ==========
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(
                        R.string.product_code_label_format,
                        product.code
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Badge danh mục + badge low-stock
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Badge(
                        containerColor = meta.color.copy(alpha = 0.15f),
                        contentColor = meta.color
                    ) {
                        Text(
                            text = product.category,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            ),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (product.isLowStock()) {
                        Badge(
                            containerColor = StockDanger.copy(alpha = 0.12f),
                            contentColor = StockDanger
                        ) {
                            Text(
                                text = stringResource(R.string.badge_low_stock),
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // ========== CỘT PHẢI: SL, GIÁ, TỔNG GT ==========
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(
                        R.string.product_qty_unit,
                        product.quantity
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (product.isLowStock()) {
                        StockDanger  // Đỏ nếu tồn kho thấp
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = formatCurrency(product.price),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(
                        R.string.product_total_value_label,
                        formatCompactCurrency(
                            product.quantity * product.price
                        )
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
