package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Product
import com.example.ui.theme.*
import java.io.File
import java.text.NumberFormat
import java.util.Locale

// ── Utility ──

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("vi-VN"))
    return format.format(amount)
}

fun formatCompactCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000_000 -> String.format(Locale.US, "%.1f tỷ", amount / 1_000_000_000.0)
        amount >= 1_000_000 -> String.format(Locale.US, "%.1f tr", amount / 1_000_000.0)
        amount >= 1_000 -> String.format(Locale.US, "%.1f n", amount / 1_000.0)
        else -> String.format(Locale.US, "%.0f", amount)
    }
}

fun formatCompactNumber(number: Int): String {
    return when {
        number >= 1_000_000 -> String.format(Locale.US, "%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format(Locale.US, "%.1fn", number / 1_000.0)
        else -> number.toString()
    }
}

fun Product.isLowStock(): Boolean = quantity <= LOW_STOCK_THRESHOLD

// ── Category ──

data class CategoryMeta(val name: String, val icon: ImageVector, val color: Color, val description: String)

object CategoryRegistry {
    val categories = listOf(
        CategoryMeta("Thư mục", Icons.Rounded.Folder, CategoryFolders, "Danh mục mặc định"),
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

// ── Shared Composables ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseHeader(title: String, showActions: Boolean = true, onActionClick: () -> Unit = {}) {
    TopAppBar(
        title = { Text(text = title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary) },
        actions = {
            if (showActions) {
                IconButton(onClick = onActionClick) {
                    Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Thêm lựa chọn", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.statusBarsPadding()
    )
}

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

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            if (action != null) {
                Spacer(modifier = Modifier.height(12.dp))
                action()
            }
        }
    }
}

@Composable
fun WarehouseCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
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

enum class StatVariant { Compact, Rich }

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    variant: StatVariant = StatVariant.Compact,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    when (variant) {
        StatVariant.Compact -> {
            Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        StatVariant.Rich -> {
            WarehouseCard(modifier = modifier) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                    if (icon != null) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                    } else {
                        Text(text = value, style = MaterialTheme.typography.labelLarge, color = iconTint, textAlign = TextAlign.Center)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (icon != null) {
                    Text(text = value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

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
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = StockDanger,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$productName (Mã: $productCode) sắp hết hàng!",
                style = MaterialTheme.typography.bodyMedium,
                color = StockDanger,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$quantity cái",
                style = MaterialTheme.typography.labelLarge,
                color = StockDanger
            )
        }
    }
}

@Composable
fun WarehouseSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Tìm tên, mã hoặc danh mục..."
) {
    WarehouseCard(modifier = modifier) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Tìm kiếm",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Xóa tìm kiếm",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun WarehouseDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}

// ── Product List Item ──

@Composable
fun ProductListItem(product: Product, onClick: () -> Unit, onEditClick: () -> Unit, onAdjustStock: (Int) -> Unit) {
    val meta = CategoryRegistry.getMeta(product.category)

    WarehouseCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(meta.color.copy(alpha = 0.12f)),
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
                    Icon(imageVector = meta.icon, contentDescription = product.category, tint = meta.color, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "Mã: ${product.code}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = meta.color.copy(alpha = 0.15f), contentColor = meta.color) {
                        Text(text = product.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (product.isLowStock()) {
                        Badge(containerColor = StockDanger.copy(alpha = 0.12f), contentColor = StockDanger) {
                            Text(text = "Sắp hết", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                Text(
                    text = "${product.quantity} cái",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (product.isLowStock()) StockDanger else MaterialTheme.colorScheme.onSurface
                )
                Text(text = formatCurrency(product.price), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Trị giá: ${formatCompactCurrency(product.quantity * product.price)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Menu Button ──

@Composable
fun WarehouseMenuButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
