package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Product
import com.example.ui.theme.*
import java.io.File
import java.text.NumberFormat
import java.util.*

// Formats pricing nicely
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}

// Compact currency formatting (e.g. $36M, $36.36k)
fun formatCompactCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000_000 -> String.format(Locale.US, "$%.1fB", amount / 1_000_000_000.0)
        amount >= 1_000_000 -> String.format(Locale.US, "$%.1fM", amount / 1_000_000.0)
        amount >= 1_000 -> String.format(Locale.US, "$%.1fk", amount / 1_000.0)
        else -> String.format(Locale.US, "$%.2f", amount)
    }
}

// Compact number formatting (e.g. 3.60k)
fun formatCompactNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.2fk", number / 1_000.0)
        else -> number.toString()
    }
}

// Category design mapping
data class CategoryMeta(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

object CategoryRegistry {
    val categories = listOf(
        CategoryMeta("Folders", Icons.Rounded.Folder, CategoryFolders, "Danh mục Folders mặc định thiết kế"),
        CategoryMeta("Thời trang", Icons.Rounded.Checkroom, CategoryFashion, "Quần áo, phụ kiện, giày dép"),
        CategoryMeta("Công nghệ", Icons.Rounded.Devices, CategoryTech, "Thiết bị điện tử, phần cứng máy tính"),
        CategoryMeta("Thực phẩm", Icons.Rounded.Restaurant, CategoryFood, "Đồ ăn thức uống, hàng tiêu dùng"),
        CategoryMeta("Chung", Icons.Rounded.Category, CategoryGeneral, "Các loại mặt hàng kho tổng hợp")
    )

    fun getMeta(categoryName: String): CategoryMeta {
        return categories.find { it.name.equals(categoryName, ignoreCase = true) }
            ?: CategoryMeta(categoryName, Icons.Rounded.Category, CategoryGeneral, "Phân loại chung")
    }
}

// Red top-bar with custom actions, matches layout exactly
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseHeader(
    title: String,
    showActions: Boolean = true,
    onActionClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        actions = {
            if (showActions) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Thêm lựa chọn",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = WarehouseRed
        ),
        modifier = Modifier.statusBarsPadding()
    )
}

// Interactive Product Card item shown in lists
@Composable
fun ProductListItem(
    product: Product,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onAdjustStock: (Int) -> Unit
) {
    val meta = CategoryRegistry.getMeta(product.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .strongGlassShine(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left image display
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(meta.color.copy(alpha = 0.15f)),
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
                    Icon(
                        imageVector = meta.icon,
                        contentDescription = product.category,
                        tint = meta.color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Mã: ${product.code}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Badge(
                        containerColor = meta.color.copy(alpha = 0.2f),
                        contentColor = meta.color
                    ) {
                        Text(
                            text = product.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Low stock red alert flag
                    if (product.quantity <= 10) {
                        Badge(
                            containerColor = WarehouseRed.copy(alpha = 0.15f),
                            contentColor = WarehouseRed
                        ) {
                            Text(
                                text = "Sắp Hết",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Right pricing / qty display and controls
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${product.quantity} chiếc",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (product.quantity <= 10) WarehouseRed else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatCurrency(product.price),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Text(
                    text = "Trị giá: ${formatCompactCurrency(product.quantity * product.price)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun WarehouseMenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF333333)
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
