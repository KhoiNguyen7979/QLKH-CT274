package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Product
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.utils.*

/**
 * Màn hình Dashboard (Tab 0) - Tổng quan kho hàng.
 * Hiển thị:
 * 1. Tổng giá trị tồn kho
 * 2. Thống kê (số SP, thư mục, tổng SL, tổng GT)
 * 3. Sản phẩm gần đây (horizontal scroll)
 * 4. Cảnh báo tồn kho thấp
 */
@Composable
fun DashboardScreen(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    // Tính toán thống kê từ danh sách sản phẩm
    val totalUniqueItems = products.size
    val totalCategories = products.map { it.category }.distinct().size
    val totalQuantity = products.sumOf { it.quantity }
    val totalValue = products.sumOf { it.quantity * it.price }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // ========== CARD TỔNG GIÁ TRỊ TỒN KHO ==========
        WarehouseCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_total_value_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(totalValue),  // Hiển thị theo định dạng VND
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ========== SECTION THỐNG KÊ TỔNG QUAN ==========
        SectionHeader(
            title = stringResource(R.string.dashboard_overview_title),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // 4 StatCard hiển thị thống kê: SP, thư mục, tổng SL, tổng GT
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                label = stringResource(R.string.stat_products),
                value = totalUniqueItems.toString(),
                modifier = Modifier.weight(1f),
                variant = StatVariant.Rich,
                icon = Icons.Rounded.Folder,
                iconTint = CategoryFolders
            )
            StatCard(
                label = stringResource(R.string.stat_folders),
                value = totalCategories.toString(),
                modifier = Modifier.weight(1f),
                variant = StatVariant.Rich,
                icon = Icons.Rounded.Folder,
                iconTint = CategoryFashion
            )
            StatCard(
                label = stringResource(R.string.stat_total_qty),
                value = formatCompactNumber(totalQuantity),  // Rút gọn (M/n)
                modifier = Modifier.weight(1f),
                variant = StatVariant.Rich,
                icon = Icons.Rounded.Folder,
                iconTint = CategoryFood
            )
            StatCard(
                label = stringResource(R.string.stat_total_value),
                value = formatCompactCurrency(totalValue),  // Rút gọn (tỷ/tr/n)
                modifier = Modifier.weight(1f),
                variant = StatVariant.Rich,
                icon = Icons.Rounded.AttachMoney,
                iconTint = CategoryTech
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ========== SECTION SẢN PHẨM GẦN ĐÂY ==========
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = stringResource(R.string.section_recent_products))
            // Link "Xem tất cả" → chuyển sang tab Inventory (tab 1)
            Text(
                text = stringResource(R.string.btn_view_all),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToTab(1) }
            )
        }

        if (products.isEmpty()) {
            // Trống: hiển thị EmptyState
            EmptyState(
                icon = Icons.Rounded.Folder,
                title = stringResource(R.string.dashboard_empty_title),
                subtitle = stringResource(R.string.dashboard_empty_subtitle),
                modifier = Modifier.height(200.dp)
            )
        } else {
            // Hiển thị 6 sản phẩm gần nhất dạng horizontal scroll
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products.take(6)) { product ->
                    RecentItemCard(product = product, onClick = { onProductClick(product) })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ========== SECTION CẢNH BÁO TỒN KHO THẤP ==========
        val lowStockProducts = products.filter { it.isLowStock() }
        if (lowStockProducts.isNotEmpty()) {
            SectionHeader(
                title = stringResource(R.string.section_low_stock_warning),
                color = StockDanger,  // Màu đỏ cho cảnh báo
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                lowStockProducts.forEach { product ->
                    LowStockWarning(
                        productName = product.name,
                        productCode = product.code,
                        quantity = product.quantity,
                        onClick = { onProductClick(product) },  // Click → xem chi tiết
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))  // Padding cuối cho bottom bar
    }
}
