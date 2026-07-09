package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.WarehouseRed

// Định nghĩa enum cho các tùy chọn trạng thái tồn kho nâng cao
enum class StockFilter { ALL, LOW_STOCK, HIGH_STOCK }
// Định nghĩa enum cho các tiêu chí sắp xếp nâng cao
enum class SortCriteria { NONE, PRICE_ASC, PRICE_DESC, QTY_ASC, QTY_DESC }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsListScreen(
    products: List<Product>,
    filteredProducts: List<Product>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onAddProductClick: () -> Unit,
    onAdjustStock: (Product, Int) -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // FEATURE NÂNG CẤP: Lưu trạng thái filter tồn kho và tiêu chí sắp xếp
    var currentStockFilter by remember { mutableStateOf(StockFilter.ALL) }
    var currentSortCriteria by remember { mutableStateOf(SortCriteria.NONE) }

    // Xử lý tập hợp tất cả điều kiện lọc và sắp xếp nâng cao vào danh sách hiển thị
    val finalDisplayedProducts = remember(filteredProducts, currentStockFilter, currentSortCriteria) {
        var list = filteredProducts

        // 1. Áp dụng bộ lọc trạng thái tồn kho
        list = when (currentStockFilter) {
            StockFilter.LOW_STOCK -> list.filter { it.quantity <= 8 }
            StockFilter.HIGH_STOCK -> list.filter { it.quantity >= 50 }
            StockFilter.ALL -> list
        }

        // 2. Áp dụng tiêu chí sắp xếp đa năng
        when (currentSortCriteria) {
            SortCriteria.PRICE_ASC -> list.sortedBy { it.price }
            SortCriteria.PRICE_DESC -> list.sortedByDescending { it.price }
            SortCriteria.QTY_ASC -> list.sortedBy { it.quantity }
            SortCriteria.QTY_DESC -> list.sortedByDescending { it.quantity }
            SortCriteria.NONE -> list
        }
    }

    val categoriesList = listOf("Tất cả") + products.map { it.category }.distinct()
    val foldersCount = products.map { it.category }.distinct().size
    val itemsCount = products.size
    val totalQty = products.sumOf { it.quantity }
    val totalVal = products.sumOf { it.quantity * it.price }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Stats Strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStatItem(title = "Folders", value = foldersCount.toString(), modifier = Modifier.weight(1f))
                    DividerVertical()
                    MiniStatItem(title = "Items", value = itemsCount.toString(), modifier = Modifier.weight(1f))
                    DividerVertical()
                    MiniStatItem(title = "Tổng SL", value = formatCompactNumber(totalQty), modifier = Modifier.weight(1f))
                    DividerVertical()
                    MiniStatItem(title = "Tổng trị giá", value = formatCompactCurrency(totalVal), modifier = Modifier.weight(1f))
                }
            }

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    placeholder = { Text("Tìm kiếm sản phẩm, mã hoặc danh mục...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search icon",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            // Category Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoriesList) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarehouseRed,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            // ITEMS LIST Title & Filter Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ITEMS LIST (${finalDisplayedProducts.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { showFilterSheet = true }
                        .padding(4.dp)
                ) {
                    val isFiltering = currentStockFilter != StockFilter.ALL || currentSortCriteria != SortCriteria.NONE
                    Icon(
                        imageVector = Icons.Rounded.FilterList,
                        contentDescription = "Filter",
                        tint = if (isFiltering) WarehouseRed else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Filter",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFiltering) WarehouseRed else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            if (finalDisplayedProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Không tìm thấy sản phẩm nào",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(finalDisplayedProducts, key = { it.id }) { product ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shineEffect() // Vệt kính gương chạy đè trên cùng bề mặt
                        ) {
                            ProductListItem(
                                product = product,
                                onClick = { onProductClick(product) },
                                onEditClick = { },
                                onAdjustStock = { amount -> onAdjustStock(product, amount) }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddProductClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 20.dp),
            containerColor = WarehouseRed,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Thêm sản phẩm mới"
            )
        }
    }

    // FEATURE NÂNG CẤP: ModalBottomSheet chứa đầy đủ bộ lọc và sắp xếp nâng cao đa năng
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 40.dp)
            ) {
                Text(
                    text = "Bộ lọc & Sắp xếp nâng cao",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 1. Khối lọc Trạng thái tồn kho (Sử dụng các hàng Filter Chips đa chọn)
                Text("Trạng thái tồn kho", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = currentStockFilter == StockFilter.ALL,
                        onClick = { currentStockFilter = StockFilter.ALL },
                        label = { Text("Tất cả") }
                    )
                    FilterChip(
                        selected = currentStockFilter == StockFilter.LOW_STOCK,
                        onClick = { currentStockFilter = StockFilter.LOW_STOCK },
                        label = { Text("Sắp hết hàng (≤ 8)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = WarehouseRed.copy(alpha = 0.15f), selectedLabelColor = WarehouseRed)
                    )
                    FilterChip(
                        selected = currentStockFilter == StockFilter.HIGH_STOCK,
                        onClick = { currentStockFilter = StockFilter.HIGH_STOCK },
                        label = { Text("Tồn nhiều (≥ 50)") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                // 2. Khối Sắp xếp đa tiêu chí (Theo Giá sản phẩm)
                Text("Sắp xếp theo giá", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatedFilterChip(
                        selected = currentSortCriteria == SortCriteria.PRICE_ASC,
                        onClick = { currentSortCriteria = if (currentSortCriteria == SortCriteria.PRICE_ASC) SortCriteria.NONE else SortCriteria.PRICE_ASC },
                        label = { Text("Giá: Thấp → Cao") }
                    )
                    ElevatedFilterChip(
                        selected = currentSortCriteria == SortCriteria.PRICE_DESC,
                        onClick = { currentSortCriteria = if (currentSortCriteria == SortCriteria.PRICE_DESC) SortCriteria.NONE else SortCriteria.PRICE_DESC },
                        label = { Text("Giá: Cao → Thấp") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Khối Sắp xếp đa tiêu chí (Theo Số lượng tồn kho)
                Text("Sắp xếp theo số lượng", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatedFilterChip(
                        selected = currentSortCriteria == SortCriteria.QTY_DESC,
                        onClick = { currentSortCriteria = if (currentSortCriteria == SortCriteria.QTY_DESC) SortCriteria.NONE else SortCriteria.QTY_DESC },
                        label = { Text("Tồn kho: Nhiều nhất") }
                    )
                    ElevatedFilterChip(
                        selected = currentSortCriteria == SortCriteria.QTY_ASC,
                        onClick = { currentSortCriteria = if (currentSortCriteria == SortCriteria.QTY_ASC) SortCriteria.NONE else SortCriteria.QTY_ASC },
                        label = { Text("Tồn kho: Ít nhất") }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Nút Xác nhận và Đóng sheet bộ lọc
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = WarehouseRed)
                ) {
                    Text("Áp dụng bộ lọc", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// SỬA ĐỔI TOÀN DIỆN: Hiệu ứng quét mặt kính (Gương phản chiếu) bóng bẩy nổi hẳn lên trên cùng bề mặt
fun Modifier.shineEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "ShineTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1300f, // Tăng biên độ quét để vệt kính đi hết các dòng item dài rộng
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing), // Quét mượt trong vòng 2 giây
            repeatMode = RepeatMode.Restart
        ),
        label = "ShineAnimation"
    )

    val shineColors = listOf(
        Color.Transparent,
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.38f), // Tăng độ sáng lõi vệt gương cho rõ nét mặt kính
        Color.White.copy(alpha = 0.05f),
        Color.Transparent
    )

    // Dùng drawWithContent ép lớp ánh kính này luôn vẽ đè sau khi nội dung card đã render xong
    this.drawWithContent {
        drawContent() // Vẽ chữ, ảnh, card trước
        drawRect(     // Quét vệt gương sáng loáng lên bề mặt
            brush = Brush.linearGradient(
                colors = shineColors,
                start = Offset(translateAnim, 0f),
                end = Offset(translateAnim + 260f, size.height)
            )
        )
    }
}