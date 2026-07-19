package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Product
import com.example.ui.theme.*
import java.io.File

@Composable
fun DashboardScreen(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

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

        WarehouseCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "TỔNG TRỊ GIÁ TỒN KHO",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(totalValue),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(
            title = "TỔNG QUAN KHO HÀNG",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(label = "Sản phẩm", value = totalUniqueItems.toString(), modifier = Modifier.weight(1f), variant = StatVariant.Rich, icon = Icons.Rounded.Folder, iconTint = CategoryFolders)
            StatCard(label = "Thư mục", value = totalCategories.toString(), modifier = Modifier.weight(1f), variant = StatVariant.Rich, icon = Icons.Rounded.Folder, iconTint = CategoryFashion)
            StatCard(label = "Tổng SL", value = formatCompactNumber(totalQuantity), modifier = Modifier.weight(1f), variant = StatVariant.Rich, icon = Icons.Rounded.Folder, iconTint = CategoryFood)
            StatCard(label = "Trị giá", value = formatCompactCurrency(totalValue), modifier = Modifier.weight(1f), variant = StatVariant.Rich, icon = Icons.Rounded.AttachMoney, iconTint = CategoryTech)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(title = "SẢN PHẨM GẦN ĐÂY")
            Text(
                text = "Xem tất cả",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToTab(1) }
            )
        }

        if (products.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Folder,
                title = "Không có sản phẩm nào",
                subtitle = "Thêm sản phẩm để bắt đầu quản lý kho hàng",
                modifier = Modifier.height(200.dp)
            )
        } else {
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

        val lowStockProducts = products.filter { it.isLowStock() }
        if (lowStockProducts.isNotEmpty()) {
            SectionHeader(
                title = "CẢNH BÁO TỒN KHO THẤP",
                color = StockDanger,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                lowStockProducts.forEach { product ->
                    LowStockWarning(
                        productName = product.name,
                        productCode = product.code,
                        quantity = product.quantity,
                        onClick = { onProductClick(product) },
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun RecentItemCard(product: Product, onClick: () -> Unit) {
    val meta = CategoryRegistry.getMeta(product.category)

    WarehouseCard(
        modifier = Modifier.width(150.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(meta.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrls.isNotEmpty() && !product.imageUrls.first().startsWith("preset_")) {
                    Image(
                        painter = rememberAsyncImagePainter(File(product.imageUrls.first())),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(imageVector = meta.icon, contentDescription = product.name, tint = meta.color, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Mã: ${product.code.take(6)}...", style = MaterialTheme.typography.labelSmall, color = meta.color)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${product.quantity} cái",
                style = MaterialTheme.typography.labelLarge,
                color = if (product.isLowStock()) StockDanger else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
