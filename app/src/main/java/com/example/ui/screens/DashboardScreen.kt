package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val foldersCount = products.map { it.category }.distinct().size
    val itemsCount = products.size
    val totalQty = products.sumOf { it.quantity }
    val totalVal = products.sumOf { it.quantity * it.price }
    
    val lowStockProducts = products.filter { it.quantity <= 8 }.sortedBy { it.quantity }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Tổng quan kho hàng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Stats Grid
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(
                            title = "Danh mục",
                            value = foldersCount.toString(),
                            icon = Icons.Rounded.Category,
                            color = CategoryFolders,
                            modifier = Modifier.weight(1f).clickable { onNavigateToTab(1) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        StatCard(
                            title = "Sản phẩm",
                            value = itemsCount.toString(),
                            icon = Icons.Rounded.Inventory2,
                            color = WarehouseRed,
                            modifier = Modifier.weight(1f).clickable { onNavigateToTab(1) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(
                            title = "Tổng tồn kho",
                            value = formatCompactNumber(totalQty),
                            icon = Icons.Rounded.Numbers,
                            color = Color(0xFF673AB7),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        StatCard(
                            title = "Tổng giá trị",
                            value = formatCompactCurrency(totalVal),
                            icon = Icons.Rounded.AccountBalanceWallet,
                            color = Color(0xFF009688),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Quick Actions
        item {
            Text(
                text = "Truy cập nhanh",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionItem(
                    text = "Tìm kiếm",
                    icon = Icons.Rounded.Search,
                    onClick = { onNavigateToTab(3) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionItem(
                    text = "Thông báo",
                    icon = Icons.Rounded.Notifications,
                    onClick = { onNavigateToTab(2) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Low Stock Warning
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sắp hết hàng (${lowStockProducts.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (lowStockProducts.isNotEmpty()) WarehouseRed else MaterialTheme.colorScheme.onBackground
                )
                if (lowStockProducts.size > 3) {
                    Text(
                        text = "Xem tất cả",
                        fontSize = 12.sp,
                        color = WarehouseRed,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToTab(1) }
                    )
                }
            }
        }

        if (lowStockProducts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Mọi thứ đều ổn, tồn kho đầy đủ!", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(lowStockProducts.take(5), key = { "low_${it.id}" }) { product ->
                ProductListItem(
                    product = product,
                    onClick = { onProductClick(product) },
                    onEditClick = { },
                    onAdjustStock = { }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun QuickActionItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
