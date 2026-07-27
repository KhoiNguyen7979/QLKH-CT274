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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.Product
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.utils.*
import java.io.File

/**
 * Màn hình Chi tiết Sản phẩm.
 * Hiển thị:
 * 1. Ảnh sản phẩm (HorizontalPager, swipe ngang)
 * 2. Điều chỉnh tồn kho (+1, -1, +10, -10)
 * 3. Thông tin chi tiết (giá, mã SKU, danh mục, tổng GT)
 * 4. Mô tả sản phẩm
 * 5. Nút xóa sản phẩm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAdjustStock: (Int) -> Unit
) {
    // Nếu product null (không tìm thấy) → hiển thị thông báo lỗi
    if (product == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.product_not_found),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val meta = CategoryRegistry.getMeta(product.category)  // Lấy icon + color theo danh mục
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    // Nút quay lại (circle button)
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
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // Nút chỉnh sửa (circle button)
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                            .clickable { onEditClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.content_desc_edit),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
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
            // ========== PHẦN ẢNH SẢN PHẨM (HorizontalPager) ==========
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
                    // Hiển thị ảnh từ URLs (swipe ngang)
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val url = product.imageUrls[page]
                        if (url.startsWith("preset_")) {
                            // Ảnh preset → hiển thị icon danh mục
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = meta.icon,
                                    contentDescription = null,
                                    tint = meta.color,
                                    modifier = Modifier.size(100.dp)
                                )
                            }
                        } else {
                            // Ảnh thật từ file
                            Image(
                                painter = rememberAsyncImagePainter(File(url)),
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    // Dots indicator (chỉ hiển thị nếu có nhiều hơn 1 ảnh)
                    if (product.imageUrls.size > 1) {
                        Row(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(product.imageUrls.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(8.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Không có ảnh → hiển thị icon danh mục lớn
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(meta.color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = meta.icon,
                                contentDescription = product.name,
                                tint = meta.color,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.product_code_label, product.code),
                            style = MaterialTheme.typography.titleSmall,
                            color = meta.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== PHẦN ĐIỀU CHỈNH TỒN KHO ==========
            WarehouseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SectionHeader(title = stringResource(R.string.section_stock_adjustment))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Hàng nút điều chỉnh: -10, -1, số lượng hiện tại, +1, +10
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StockAdjButton(
                            label = "-10",
                            onClick = { onAdjustStock(-10) }
                        )

                        IconButton(
                            onClick = { onAdjustStock(-1) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = stringResource(R.string.content_desc_decrease_one)
                            )
                        }

                        // Hiển thị số lượng hiện tại
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(100.dp)
                        ) {
                            Text(
                                text = product.quantity.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                color = if (product.isLowStock())
                                    StockDanger  // Đỏ nếu tồn kho thấp
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.stock_unit_in_stock),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onAdjustStock(1) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.content_desc_increase_one),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        StockAdjButton(
                            label = "+10",
                            onClick = { onAdjustStock(10) }
                        )
                    }

                    // Hiển thị cảnh báo nếu tồn kho thấp (≤ 10)
                    if (product.isLowStock()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(StockDanger.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = stringResource(R.string.content_desc_warning),
                                tint = StockDanger,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.low_stock_alert),
                                style = MaterialTheme.typography.labelLarge,
                                color = StockDanger
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== PHẦN THÔNG TIN CHI TIẾT ==========
            WarehouseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = stringResource(R.string.section_product_details))
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailSpecRow(
                        icon = Icons.Rounded.AttachMoney,
                        label = stringResource(R.string.detail_unit_price),
                        value = formatCurrency(product.price)
                    )
                    WarehouseDivider()
                    DetailSpecRow(
                        icon = Icons.Rounded.QrCode,
                        label = stringResource(R.string.detail_sku_code),
                        value = product.code
                    )
                    WarehouseDivider()
                    DetailSpecRow(
                        icon = Icons.Rounded.Folder,
                        label = stringResource(R.string.detail_category),
                        value = product.category
                    )
                    WarehouseDivider()
                    DetailSpecRow(
                        icon = Icons.Rounded.Equalizer,
                        label = stringResource(R.string.detail_total_value),
                        value = formatCurrency(product.quantity * product.price),
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== PHẦN MÔ TẢ ==========
            WarehouseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = stringResource(R.string.section_product_description))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description.ifEmpty {
                            stringResource(R.string.no_description)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========== NÚT XÓA SẢN PHẨM ==========
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                WarehouseMenuButton(
                    text = stringResource(R.string.btn_delete_product),
                    icon = Icons.Rounded.Delete,
                    onClick = { showDeleteDialog = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // ========== DIALOG XÁC NHẬN XÓA ==========
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(stringResource(R.string.dialog_delete_title))
                },
                text = {
                    Text(
                        stringResource(
                            R.string.dialog_delete_message,
                            product.name,
                            product.code
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteClick()  // Gọi callback xóa
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            stringResource(R.string.dialog_delete_confirm),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.dialog_delete_cancel))
                    }
                }
            )
        }
    }
}
