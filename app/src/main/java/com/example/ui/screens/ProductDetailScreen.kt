package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Product
import com.example.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAdjustStock: (Int) -> Unit
) {
    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy sản phẩm!", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val meta = CategoryRegistry.getMeta(product.category)
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product.name, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Hero images
            val pagerState = rememberPagerState(pageCount = {
                if (product.imageUrls.isEmpty()) 1 else product.imageUrls.size
            })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(meta.color.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrls.isNotEmpty()) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        val url = product.imageUrls[page]
                        if (url.startsWith("preset_")) {
                            Icon(imageVector = meta.icon, contentDescription = null, tint = meta.color, modifier = Modifier.size(100.dp))
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(File(url)),
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    if (product.imageUrls.size > 1) {
                        Row(
                            Modifier.wrapContentHeight().fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(product.imageUrls.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                Box(modifier = Modifier.padding(4.dp).clip(CircleShape).background(color).size(8.dp))
                            }
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(
                            modifier = Modifier.size(100.dp).clip(CircleShape).background(meta.color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = meta.icon, contentDescription = product.name, tint = meta.color, modifier = Modifier.size(54.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Mã sản phẩm: ${product.code}", style = MaterialTheme.typography.titleSmall, color = meta.color)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity controller
            WarehouseCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    SectionHeader(title = "ĐIỀU CHỈNH TỒN KHO")
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        StockAdjButton(label = "-10", onClick = { onAdjustStock(-10) })

                        IconButton(
                            onClick = { onAdjustStock(-1) },
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Rounded.Remove, contentDescription = "Giảm 1")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
                            Text(
                                text = product.quantity.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                color = if (product.isLowStock()) StockDanger else MaterialTheme.colorScheme.primary
                            )
                            Text(text = "chiếc trong kho", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        IconButton(
                            onClick = { onAdjustStock(1) },
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Tăng 1", tint = MaterialTheme.colorScheme.primary)
                        }

                        StockAdjButton(label = "+10", onClick = { onAdjustStock(10) })
                    }

                    if (product.isLowStock()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(StockDanger.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Warning, contentDescription = "Cảnh báo", tint = StockDanger, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Cảnh báo: Hàng tồn kho sắp hết!", style = MaterialTheme.typography.labelLarge, color = StockDanger)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Specs
            WarehouseCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "THÔNG TIN CHI TIẾT")
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailSpecRow(icon = Icons.Rounded.AttachMoney, label = "Đơn giá", value = formatCurrency(product.price))
                    WarehouseDivider()
                    DetailSpecRow(icon = Icons.Rounded.QrCode, label = "Mã SKU", value = product.code)
                    WarehouseDivider()
                    DetailSpecRow(icon = Icons.Rounded.Folder, label = "Thư mục/Phân loại", value = product.category)
                    WarehouseDivider()
                    DetailSpecRow(icon = Icons.Rounded.Equalizer, label = "Tổng giá trị", value = formatCurrency(product.quantity * product.price), valueColor = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            WarehouseCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "MÔ TẢ SẢN PHẨM")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description.ifEmpty { "Không có mô tả chi tiết cho sản phẩm này." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
                WarehouseMenuButton(
                    text = "Xóa sản phẩm",
                    icon = Icons.Rounded.Delete,
                    onClick = { showDeleteDialog = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                WarehouseMenuButton(
                    text = "Chỉnh sửa sản phẩm",
                    icon = Icons.Rounded.Edit,
                    onClick = onEditClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xóa sản phẩm?") },
                text = { Text("Bạn có chắc chắn muốn xóa sản phẩm '${product.name}' (Mã: ${product.code}) khỏi kho hàng? Hành động này không thể hoàn tác.") },
                confirmButton = {
                    Button(
                        onClick = { showDeleteDialog = false; onDeleteClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Xóa bỏ", color = MaterialTheme.colorScheme.onError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Quay lại")
                    }
                }
            )
        }
    }
}

@Composable
fun StockAdjButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun DetailSpecRow(icon: ImageVector, label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = value, style = MaterialTheme.typography.titleSmall, color = valueColor, textAlign = TextAlign.End)
    }
}
