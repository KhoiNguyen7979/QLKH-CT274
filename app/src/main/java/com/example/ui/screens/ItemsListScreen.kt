package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import com.example.ui.theme.WarehouseRed

/**
 * Trạng thái tồn kho.
 */
enum class StockFilter {
    ALL,
    LOW_STOCK,
    HIGH_STOCK
}

/**
 * Tiêu chí sắp xếp.
 */
enum class SortCriteria {
    NONE,
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC,
    QTY_ASC,
    QTY_DESC
}

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

    // Thêm sự kiện sửa nhanh.
    onEditProduct: (Product) -> Unit,

    onAddProductClick: () -> Unit,
    onAdjustStock: (Product, Int) -> Unit
) {
    var showFilterSheet by rememberSaveable {
        mutableStateOf(false)
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    /*
     * Bộ lọc đang được áp dụng cho danh sách.
     */
    var appliedStockFilter by rememberSaveable {
        mutableStateOf(StockFilter.ALL)
    }

    var appliedSortCriteria by rememberSaveable {
        mutableStateOf(SortCriteria.NONE)
    }

    var appliedMinPrice by rememberSaveable {
        mutableStateOf<Double?>(null)
    }

    var appliedMaxPrice by rememberSaveable {
        mutableStateOf<Double?>(null)
    }

    var appliedMinQuantity by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var appliedMaxQuantity by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    /*
     * Danh sách cuối cùng sau khi:
     * 1. Tìm kiếm.
     * 2. Lọc danh mục.
     * 3. Lọc tồn kho.
     * 4. Lọc khoảng giá.
     * 5. Lọc khoảng số lượng.
     * 6. Sắp xếp.
     */
    val finalDisplayedProducts = remember(
        filteredProducts,
        appliedStockFilter,
        appliedSortCriteria,
        appliedMinPrice,
        appliedMaxPrice,
        appliedMinQuantity,
        appliedMaxQuantity
    ) {
        val filteredList = filteredProducts.filter { product ->
            val matchesStock = when (appliedStockFilter) {
                StockFilter.ALL -> true
                StockFilter.LOW_STOCK -> product.quantity <= 10
                StockFilter.HIGH_STOCK -> product.quantity >= 50
            }

            val matchesMinPrice =
                appliedMinPrice == null ||
                        product.price >= appliedMinPrice!!

            val matchesMaxPrice =
                appliedMaxPrice == null ||
                        product.price <= appliedMaxPrice!!

            val matchesMinQuantity =
                appliedMinQuantity == null ||
                        product.quantity >= appliedMinQuantity!!

            val matchesMaxQuantity =
                appliedMaxQuantity == null ||
                        product.quantity <= appliedMaxQuantity!!

            matchesStock &&
                    matchesMinPrice &&
                    matchesMaxPrice &&
                    matchesMinQuantity &&
                    matchesMaxQuantity
        }

        when (appliedSortCriteria) {
            SortCriteria.NONE -> filteredList

            SortCriteria.NAME_ASC ->
                filteredList.sortedBy {
                    it.name.lowercase()
                }

            SortCriteria.NAME_DESC ->
                filteredList.sortedByDescending {
                    it.name.lowercase()
                }

            SortCriteria.PRICE_ASC ->
                filteredList.sortedBy {
                    it.price
                }

            SortCriteria.PRICE_DESC ->
                filteredList.sortedByDescending {
                    it.price
                }

            SortCriteria.QTY_ASC ->
                filteredList.sortedBy {
                    it.quantity
                }

            SortCriteria.QTY_DESC ->
                filteredList.sortedByDescending {
                    it.quantity
                }
        }
    }

    val categoriesList = remember(products) {
        listOf("Tất cả") +
                products
                    .map { it.category }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
    }

    val foldersCount = remember(products) {
        products
            .map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .size
    }

    val itemsCount = products.size

    val totalQty = remember(products) {
        products.sumOf { it.quantity }
    }

    val totalVal = remember(products) {
        products.sumOf {
            it.quantity * it.price
        }
    }

    val isFiltering =
        appliedStockFilter != StockFilter.ALL ||
                appliedSortCriteria != SortCriteria.NONE ||
                appliedMinPrice != null ||
                appliedMaxPrice != null ||
                appliedMinQuantity != null ||
                appliedMaxQuantity != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            /*
             * Thẻ thống kê.
             */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    MiniStatItem(
                        title = "Folders",
                        value = foldersCount.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    DividerVertical()

                    MiniStatItem(
                        title = "Items",
                        value = itemsCount.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    DividerVertical()

                    MiniStatItem(
                        title = "Tổng SL",
                        value = formatCompactNumber(totalQty),
                        modifier = Modifier.weight(1f)
                    )

                    DividerVertical()

                    MiniStatItem(
                        title = "Tổng trị giá",
                        value = formatCompactCurrency(totalVal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            /*
             * Thanh tìm kiếm.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    placeholder = {
                        Text(
                            text = "Tìm tên, mã hoặc danh mục...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Tìm kiếm",
                            tint = MaterialTheme
                                .colorScheme
                                .onSurface
                                .copy(alpha = 0.5f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onSearchQueryChange("")
                                }
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Rounded.Clear,
                                    contentDescription =
                                        "Xóa tìm kiếm",
                                    tint = MaterialTheme
                                        .colorScheme
                                        .onSurface
                                        .copy(alpha = 0.5f)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor =
                            MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor =
                            MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor =
                            Color.Transparent,
                        unfocusedIndicatorColor =
                            Color.Transparent
                    ),
                    singleLine = true
                )
            }

            /*
             * Danh mục sản phẩm.
             */
            LazyRow(
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 4.dp
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = categoriesList,
                    key = { it }
                ) { category ->
                    val isSelected =
                        category == selectedCategory

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onCategorySelected(category)
                        },
                        label = {
                            Text(
                                text = category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor =
                                    WarehouseRed,
                                selectedLabelColor =
                                    Color.White,
                                containerColor =
                                    MaterialTheme
                                        .colorScheme
                                        .surface,
                                labelColor =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurface
                                        .copy(alpha = 0.7f)
                            )
                    )
                }
            }

            /*
             * Tiêu đề danh sách và nút lọc.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 4.dp
                    ),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        "ITEMS LIST (${finalDisplayedProducts.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme
                        .colorScheme
                        .onBackground
                        .copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            showFilterSheet = true
                        }
                        .padding(
                            horizontal = 8.dp,
                            vertical = 6.dp
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.FilterList,
                        contentDescription =
                            "Bộ lọc nâng cao",
                        tint = if (isFiltering) {
                            WarehouseRed
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onBackground
                                .copy(alpha = 0.6f)
                        },
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(4.dp)
                    )

                    Text(
                        text = if (isFiltering) {
                            "Đang lọc"
                        } else {
                            "Filter"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFiltering) {
                            WarehouseRed
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onBackground
                                .copy(alpha = 0.6f)
                        }
                    )
                }
            }

            /*
             * Không có kết quả.
             */
            if (finalDisplayedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            text =
                                "Không tìm thấy sản phẩm nào",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme
                                .colorScheme
                                .onBackground
                                .copy(alpha = 0.4f)
                        )

                        if (isFiltering) {
                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            TextButton(
                                onClick = {
                                    appliedStockFilter =
                                        StockFilter.ALL

                                    appliedSortCriteria =
                                        SortCriteria.NONE

                                    appliedMinPrice = null
                                    appliedMaxPrice = null
                                    appliedMinQuantity = null
                                    appliedMaxQuantity = null
                                }
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Rounded.Refresh,
                                    contentDescription = null
                                )

                                Spacer(
                                    modifier =
                                        Modifier.width(4.dp)
                                )

                                Text("Xóa bộ lọc")
                            }
                        }
                    }
                }
            } else {
                /*
                 * Danh sách sản phẩm.
                 */
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = finalDisplayedProducts,
                        key = { it.id }
                    ) { product ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            ProductListItem(
                                product = product,

                                onClick = {
                                    onProductClick(product)
                                },

                                // Sửa nhanh sản phẩm.
                                onEditClick = {
                                    onEditProduct(product)
                                },

                                onAdjustStock = { amount ->
                                    onAdjustStock(
                                        product,
                                        amount
                                    )
                                }
                            )
                        }
                    }

                    item {
                        Spacer(
                            modifier =
                                Modifier.height(90.dp)
                        )
                    }
                }
            }
        }

        /*
         * Nút thêm sản phẩm.
         */
        FloatingActionButton(
            onClick = onAddProductClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    bottom = 16.dp,
                    end = 20.dp
                ),
            containerColor = WarehouseRed,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription =
                    "Thêm sản phẩm mới"
            )
        }
    }

    /*
     * Bottom Sheet bộ lọc nâng cao.
     */
    if (showFilterSheet) {
        AdvancedProductFilterSheet(
            sheetState = sheetState,

            currentStockFilter =
                appliedStockFilter,

            currentSortCriteria =
                appliedSortCriteria,

            currentMinPrice =
                appliedMinPrice,

            currentMaxPrice =
                appliedMaxPrice,

            currentMinQuantity =
                appliedMinQuantity,

            currentMaxQuantity =
                appliedMaxQuantity,

            onDismiss = {
                showFilterSheet = false
            },

            onApply = {
                    stockFilter,
                    sortCriteria,
                    minPrice,
                    maxPrice,
                    minQuantity,
                    maxQuantity ->

                appliedStockFilter = stockFilter
                appliedSortCriteria = sortCriteria
                appliedMinPrice = minPrice
                appliedMaxPrice = maxPrice
                appliedMinQuantity = minQuantity
                appliedMaxQuantity = maxQuantity

                showFilterSheet = false
            },

            onClear = {
                appliedStockFilter =
                    StockFilter.ALL

                appliedSortCriteria =
                    SortCriteria.NONE

                appliedMinPrice = null
                appliedMaxPrice = null
                appliedMinQuantity = null
                appliedMaxQuantity = null

                showFilterSheet = false
            }
        )
    }
}

@Composable
fun DividerVertical() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    )
}

@Composable
fun MiniStatItem(title: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedProductFilterSheet(
    sheetState:
    androidx.compose.material3.SheetState,

    currentStockFilter: StockFilter,
    currentSortCriteria: SortCriteria,

    currentMinPrice: Double?,
    currentMaxPrice: Double?,

    currentMinQuantity: Int?,
    currentMaxQuantity: Int?,

    onDismiss: () -> Unit,

    onApply: (
        StockFilter,
        SortCriteria,
        Double?,
        Double?,
        Int?,
        Int?
    ) -> Unit,

    onClear: () -> Unit
) {
    /*
     * Trạng thái tạm trong Bottom Sheet.
     * Chỉ áp dụng khi bấm nút Áp dụng.
     */
    var tempStockFilter by remember(
        currentStockFilter
    ) {
        mutableStateOf(currentStockFilter)
    }

    var tempSortCriteria by remember(
        currentSortCriteria
    ) {
        mutableStateOf(currentSortCriteria)
    }

    var minPriceText by remember(
        currentMinPrice
    ) {
        mutableStateOf(
            currentMinPrice
                ?.let(::formatNumberInput)
                ?: ""
        )
    }

    var maxPriceText by remember(
        currentMaxPrice
    ) {
        mutableStateOf(
            currentMaxPrice
                ?.let(::formatNumberInput)
                ?: ""
        )
    }

    var minQuantityText by remember(
        currentMinQuantity
    ) {
        mutableStateOf(
            currentMinQuantity?.toString() ?: ""
        )
    }

    var maxQuantityText by remember(
        currentMaxQuantity
    ) {
        mutableStateOf(
            currentMaxQuantity?.toString() ?: ""
        )
    }

    var validationMessage by remember {
        mutableStateOf<String?>(null)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor =
            MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                bottom = 40.dp
            )
        ) {
            item {
                Text(
                    text =
                        "Bộ lọc & sắp xếp nâng cao",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            /*
             * Lọc theo tình trạng tồn kho.
             */
            item {
                FilterSectionTitle(
                    title = "Trạng thái tồn kho"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected =
                                tempStockFilter ==
                                        StockFilter.ALL,
                            onClick = {
                                tempStockFilter =
                                    StockFilter.ALL
                            },
                            label = {
                                Text("Tất cả")
                            }
                        )
                    }

                    item {
                        FilterChip(
                            selected =
                                tempStockFilter ==
                                        StockFilter.LOW_STOCK,
                            onClick = {
                                tempStockFilter =
                                    StockFilter.LOW_STOCK
                            },
                            label = {
                                Text("Sắp hết ≤ 10")
                            },
                            colors =
                                FilterChipDefaults
                                    .filterChipColors(
                                        selectedContainerColor =
                                            WarehouseRed
                                                .copy(
                                                    alpha = 0.15f
                                                ),
                                        selectedLabelColor =
                                            WarehouseRed
                                    )
                        )
                    }

                    item {
                        FilterChip(
                            selected =
                                tempStockFilter ==
                                        StockFilter.HIGH_STOCK,
                            onClick = {
                                tempStockFilter =
                                    StockFilter.HIGH_STOCK
                            },
                            label = {
                                Text("Tồn nhiều ≥ 50")
                            }
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                HorizontalDivider(
                    color = MaterialTheme
                        .colorScheme
                        .onSurface
                        .copy(alpha = 0.08f)
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }

            /*
             * Khoảng giá.
             */
            item {
                FilterSectionTitle(
                    title = "Khoảng giá sản phẩm"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minPriceText,
                        onValueChange = { value ->
                            minPriceText =
                                sanitizeDecimalInput(value)
                            validationMessage = null
                        },
                        label = {
                            Text("Giá từ")
                        },
                        placeholder = {
                            Text("0")
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Decimal
                            )
                    )

                    OutlinedTextField(
                        value = maxPriceText,
                        onValueChange = { value ->
                            maxPriceText =
                                sanitizeDecimalInput(value)
                            validationMessage = null
                        },
                        label = {
                            Text("Giá đến")
                        },
                        placeholder = {
                            Text("Không giới hạn")
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Decimal
                            )
                    )
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }

            /*
             * Khoảng số lượng.
             */
            item {
                FilterSectionTitle(
                    title = "Khoảng số lượng tồn kho"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minQuantityText,
                        onValueChange = { value ->
                            minQuantityText =
                                value.filter {
                                    it.isDigit()
                                }

                            validationMessage = null
                        },
                        label = {
                            Text("Số lượng từ")
                        },
                        placeholder = {
                            Text("0")
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            )
                    )

                    OutlinedTextField(
                        value = maxQuantityText,
                        onValueChange = { value ->
                            maxQuantityText =
                                value.filter {
                                    it.isDigit()
                                }

                            validationMessage = null
                        },
                        label = {
                            Text("Số lượng đến")
                        },
                        placeholder = {
                            Text("Không giới hạn")
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Number
                            )
                    )
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                HorizontalDivider(
                    color = MaterialTheme
                        .colorScheme
                        .onSurface
                        .copy(alpha = 0.08f)
                )

                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }

            /*
             * Sắp xếp theo tên.
             */
            item {
                FilterSectionTitle(
                    title = "Sắp xếp theo tên"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                ScrollableChipRow {
                    FilterChip(
                        selected =
                            tempSortCriteria ==
                                    SortCriteria.NAME_ASC,
                        onClick = {
                            tempSortCriteria =
                                toggleSortCriteria(
                                    current =
                                        tempSortCriteria,
                                    selected =
                                        SortCriteria.NAME_ASC
                                )
                        },
                        label = {
                            Text("Tên A → Z")
                        }
                    )

                    FilterChip(
                        selected =
                            tempSortCriteria ==
                                    SortCriteria.NAME_DESC,
                        onClick = {
                            tempSortCriteria =
                                toggleSortCriteria(
                                    current =
                                        tempSortCriteria,
                                    selected =
                                        SortCriteria.NAME_DESC
                                )
                        },
                        label = {
                            Text("Tên Z → A")
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            /*
             * Sắp xếp theo giá.
             */
            item {
                FilterSectionTitle(
                    title = "Sắp xếp theo giá"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                ScrollableChipRow {
                    FilterChip(
                        selected =
                            tempSortCriteria ==
                                    SortCriteria.PRICE_ASC,
                        onClick = {
                            tempSortCriteria =
                                toggleSortCriteria(
                                    current =
                                        tempSortCriteria,
                                    selected =
                                        SortCriteria.PRICE_ASC
                                )
                        },
                        label = {
                            Text("Giá thấp → cao")
                        }
                    )

                    FilterChip(
                        selected =
                            tempSortCriteria ==
                                    SortCriteria.PRICE_DESC,
                        onClick = {
                            tempSortCriteria =
                                toggleSortCriteria(
                                    current =
                                        tempSortCriteria,
                                    selected =
                                        SortCriteria.PRICE_DESC
                                )
                        },
                        label = {
                            Text("Giá cao → thấp")
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            /*
             * Sắp xếp theo số lượng.
             */
            item {
                FilterSectionTitle(
                    title = "Sắp xếp theo số lượng"
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                ScrollableChipRow {
                    FilterChip(
                        selected =
                            tempSortCriteria ==
                                    SortCriteria.QTY_ASC,
                        onClick = {
                            tempSortCriteria =
                                toggleSortCriteria(
                                    current =
                                        tempSortCriteria,
                                    selected =
                                        SortCriteria.QTY_ASC
                                )
                        },
                        label = {
                            Text("Ít → nhiều")
                        }
                    )

                    FilterChip(
                        selected =
                            tempSortCriteria ==
                                    SortCriteria.QTY_DESC,
                        onClick = {
                            tempSortCriteria =
                                toggleSortCriteria(
                                    current =
                                        tempSortCriteria,
                                    selected =
                                        SortCriteria.QTY_DESC
                                )
                        },
                        label = {
                            Text("Nhiều → ít")
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )
            }

            /*
             * Hiển thị lỗi nhập khoảng.
             */
            if (validationMessage != null) {
                item {
                    Text(
                        text =
                            validationMessage.orEmpty(),
                        color =
                            MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }
            }

            /*
             * Nút xóa và áp dụng.
             */
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onClear,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Refresh,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(4.dp)
                        )

                        Text("Xóa bộ lọc")
                    }

                    Button(
                        onClick = {
                            val minPrice =
                                minPriceText
                                    .replace(",", ".")
                                    .toDoubleOrNull()

                            val maxPrice =
                                maxPriceText
                                    .replace(",", ".")
                                    .toDoubleOrNull()

                            val minQuantity =
                                minQuantityText
                                    .toIntOrNull()

                            val maxQuantity =
                                maxQuantityText
                                    .toIntOrNull()

                            validationMessage =
                                validateFilterRanges(
                                    minPrice = minPrice,
                                    maxPrice = maxPrice,
                                    minQuantity =
                                        minQuantity,
                                    maxQuantity =
                                        maxQuantity
                                )

                            if (
                                validationMessage == null
                            ) {
                                onApply(
                                    tempStockFilter,
                                    tempSortCriteria,
                                    minPrice,
                                    maxPrice,
                                    minQuantity,
                                    maxQuantity
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    WarehouseRed
                            )
                    ) {
                        Text(
                            text = "Áp dụng",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSectionTitle(
    title: String
) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme
            .colorScheme
            .onSurface
            .copy(alpha = 0.65f)
    )
}

@Composable
private fun ScrollableChipRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp),
        content = content
    )
}

private fun toggleSortCriteria(
    current: SortCriteria,
    selected: SortCriteria
): SortCriteria {
    return if (current == selected) {
        SortCriteria.NONE
    } else {
        selected
    }
}

private fun validateFilterRanges(
    minPrice: Double?,
    maxPrice: Double?,
    minQuantity: Int?,
    maxQuantity: Int?
): String? {
    if (
        minPrice != null &&
        minPrice < 0
    ) {
        return "Giá tối thiểu không được nhỏ hơn 0."
    }

    if (
        maxPrice != null &&
        maxPrice < 0
    ) {
        return "Giá tối đa không được nhỏ hơn 0."
    }

    if (
        minPrice != null &&
        maxPrice != null &&
        minPrice > maxPrice
    ) {
        return "Giá từ không được lớn hơn giá đến."
    }

    if (
        minQuantity != null &&
        minQuantity < 0
    ) {
        return "Số lượng tối thiểu không hợp lệ."
    }

    if (
        maxQuantity != null &&
        maxQuantity < 0
    ) {
        return "Số lượng tối đa không hợp lệ."
    }

    if (
        minQuantity != null &&
        maxQuantity != null &&
        minQuantity > maxQuantity
    ) {
        return "Số lượng từ không được lớn hơn số lượng đến."
    }

    return null
}

private fun sanitizeDecimalInput(
    value: String
): String {
    val normalized = value
        .replace(",", ".")
        .filter {
            it.isDigit() || it == '.'
        }

    val firstDotIndex =
        normalized.indexOf('.')

    return if (firstDotIndex == -1) {
        normalized
    } else {
        normalized.substring(
            0,
            firstDotIndex + 1
        ) +
                normalized
                    .substring(firstDotIndex + 1)
                    .replace(".", "")
    }
}

private fun formatNumberInput(
    value: Double
): String {
    return if (
        value % 1.0 == 0.0
    ) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}
