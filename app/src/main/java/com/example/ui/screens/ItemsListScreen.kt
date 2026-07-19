package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Product
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.utils.*

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
    onEditProduct: (Product) -> Unit,
    onAddProductClick: () -> Unit,
    onAdjustStock: (Product, Int) -> Unit
) {
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var appliedStockFilter by rememberSaveable { mutableStateOf(StockFilter.ALL) }
    var appliedSortCriteria by rememberSaveable { mutableStateOf(SortCriteria.NONE) }
    var appliedMinPrice by rememberSaveable { mutableStateOf<Double?>(null) }
    var appliedMaxPrice by rememberSaveable { mutableStateOf<Double?>(null) }
    var appliedMinQuantity by rememberSaveable { mutableStateOf<Int?>(null) }
    var appliedMaxQuantity by rememberSaveable { mutableStateOf<Int?>(null) }

    val finalDisplayedProducts = remember(
        filteredProducts, appliedStockFilter, appliedSortCriteria,
        appliedMinPrice, appliedMaxPrice, appliedMinQuantity, appliedMaxQuantity
    ) {
        applyAdvancedFilters(
            filteredProducts, appliedStockFilter, appliedSortCriteria,
            appliedMinPrice, appliedMaxPrice, appliedMinQuantity, appliedMaxQuantity
        )
    }

    val categoriesList = remember(products) {
        listOf("Tất cả") + products.map { it.category }
            .filter { it.isNotBlank() }.distinct().sorted()
    }

    val foldersCount = remember(products) {
        products.map { it.category }.filter { it.isNotBlank() }.distinct().size
    }
    val itemsCount = products.size
    val totalQty = remember(products) { products.sumOf { it.quantity } }
    val totalVal = remember(products) { products.sumOf { it.quantity * it.price } }

    val isFiltering = appliedStockFilter != StockFilter.ALL ||
        appliedSortCriteria != SortCriteria.NONE ||
        appliedMinPrice != null || appliedMaxPrice != null ||
        appliedMinQuantity != null || appliedMaxQuantity != null

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            WarehouseCard(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatCard(
                        label = stringResource(R.string.stat_folders),
                        value = foldersCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(24.dp)
                            .width(0.5.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant
                                    .copy(alpha = 0.5f)
                            )
                    )
                    StatCard(
                        label = stringResource(R.string.stat_products),
                        value = itemsCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(24.dp)
                            .width(0.5.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant
                                    .copy(alpha = 0.5f)
                            )
                    )
                    StatCard(
                        label = stringResource(R.string.stat_total_qty),
                        value = formatCompactNumber(totalQty),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .height(24.dp)
                            .width(0.5.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant
                                    .copy(alpha = 0.5f)
                            )
                    )
                    StatCard(
                        label = stringResource(R.string.stat_total_value),
                        value = formatCompactCurrency(totalVal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            WarehouseSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = categoriesList, key = { it }) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onCategorySelected(category) },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor =
                                MaterialTheme.colorScheme.primary,
                            selectedLabelColor =
                                MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = stringResource(
                        R.string.section_product_list,
                        finalDisplayedProducts.size
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { showFilterSheet = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FilterList,
                        contentDescription = stringResource(
                            R.string.content_desc_advanced_filter
                        ),
                        tint = if (isFiltering)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFiltering)
                            stringResource(R.string.filter_active_label)
                        else
                            stringResource(R.string.filter_inactive_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isFiltering)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (finalDisplayedProducts.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.Folder,
                    title = stringResource(R.string.empty_products_title),
                    subtitle = if (isFiltering)
                        stringResource(R.string.empty_products_subtitle_filtered)
                    else
                        stringResource(R.string.empty_products_subtitle),
                    modifier = Modifier.weight(1f),
                    action = if (isFiltering) {
                        {
                            TextButton(onClick = {
                                appliedStockFilter = StockFilter.ALL
                                appliedSortCriteria = SortCriteria.NONE
                                appliedMinPrice = null
                                appliedMaxPrice = null
                                appliedMinQuantity = null
                                appliedMaxQuantity = null
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.btn_clear_filter))
                            }
                        }
                    } else null
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp, vertical = 4.dp
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = finalDisplayedProducts,
                        key = { it.id }
                    ) { product ->
                        ProductListItem(
                            product = product,
                            onClick = { onProductClick(product) },
                            onEditClick = { onEditProduct(product) },
                            onAdjustStock = { amount ->
                                onAdjustStock(product, amount)
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(90.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddProductClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = MaterialTheme.shapes.large
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(
                    R.string.content_desc_add_product
                )
            )
        }
    }

    if (showFilterSheet) {
        AdvancedProductFilterSheet(
            sheetState = sheetState,
            currentStockFilter = appliedStockFilter,
            currentSortCriteria = appliedSortCriteria,
            currentMinPrice = appliedMinPrice,
            currentMaxPrice = appliedMaxPrice,
            currentMinQuantity = appliedMinQuantity,
            currentMaxQuantity = appliedMaxQuantity,
            onDismiss = { showFilterSheet = false },
            onApply = { stock, sort, minP, maxP, minQ, maxQ ->
                appliedStockFilter = stock
                appliedSortCriteria = sort
                appliedMinPrice = minP
                appliedMaxPrice = maxP
                appliedMinQuantity = minQ
                appliedMaxQuantity = maxQ
                showFilterSheet = false
            },
            onClear = {
                appliedStockFilter = StockFilter.ALL
                appliedSortCriteria = SortCriteria.NONE
                appliedMinPrice = null
                appliedMaxPrice = null
                appliedMinQuantity = null
                appliedMaxQuantity = null
                showFilterSheet = false
            }
        )
    }
}
