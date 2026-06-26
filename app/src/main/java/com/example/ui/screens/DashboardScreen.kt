package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.CategoryFolders
import com.example.ui.theme.WarehouseRed
import com.example.ui.theme.WarehouseRedLight

@Composable
fun DashboardScreen(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    // Calculate metrics
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
        // Red Hero Card at the top with a gorgeous gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(WarehouseRed, WarehouseRed.copy(alpha = 0.85f))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hệ thống Kho hàng",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "HÔM NAY",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    // Quick Action: Add Product shortcut
                    Card(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onNavigateToTab(1) }, // Go to Items tab
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.List,
                                contentDescription = "Xem danh sách",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Xem kho",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Beautiful Total Valuation
                Text(
                    text = "TỔNG TRỊ GIÁ TỒN KHO",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = formatCurrency(totalValue),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Inventory Summary section (circular summary bubbles from the mockup drawings!)
        Text(
            text = "INVENTORY SUMMARY",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            letterSpacing = 1.sp
        )

        // Row 1: Items Count & Folders/Categories Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryCircleMetricCard(
                value = totalUniqueItems.toString(),
                label = "Items",
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
            SummaryCircleMetricCard(
                value = totalCategories.toString(),
                label = "Folders",
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Total Quantity & Total Value
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryCircleMetricCard(
                value = formatCompactNumber(totalQuantity),
                label = "Total quantity",
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
            SummaryCircleMetricCard(
                value = formatCompactCurrency(totalValue),
                label = "Total value",
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent items header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT ITEMS",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Text(
                text = "Xem tất cả",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = WarehouseRed,
                modifier = Modifier.clickable { onNavigateToTab(1) } // Go to items tab
            )
        }

        // Horizontal Recent Items Carousel
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Không có sản phẩm nào gần đây",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products.take(6)) { product ->
                    RecentItemCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick low stock alert cards panel
        val lowStockProducts = products.filter { it.quantity <= 10 }
        if (lowStockProducts.isNotEmpty()) {
            Text(
                text = "CẢNH BÁO TỒN KHO THẤP",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WarehouseRed,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                letterSpacing = 1.sp
            )
            
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                lowStockProducts.forEach { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onProductClick(product) },
                        colors = CardDefaults.cardColors(containerColor = WarehouseRedLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "Cảnh báo",
                                tint = WarehouseRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${product.name} (Mã: ${product.code}) sắp hết hàng!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarehouseRed,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${product.quantity} chiếc",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = WarehouseRed
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
    }
}

// Custom Bubble summary card design matching the user's mockup circles!
@Composable
fun SummaryCircleMetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Rounded filled circular badge for the number
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CategoryFolders.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = CategoryFolders,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Carousel Card matching the visual image grid shown in Image 1: "RECENT ITEMS"
@Composable
fun RecentItemCard(
    product: Product,
    onClick: () -> Unit
) {
    val meta = CategoryRegistry.getMeta(product.category)
    
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rounded square matching user's image grid
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(meta.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = meta.icon,
                        contentDescription = product.name,
                        tint = meta.color,
                        modifier = Modifier.size(36.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Mã: ${product.code.take(6)}...",
                        fontSize = 9.sp,
                        color = meta.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "${product.quantity} Qty",
                fontSize = 11.sp,
                color = if (product.quantity <= 10) WarehouseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
