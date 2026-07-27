package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Product
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Màn hình Tìm kiếm (Tab 3).
 * Cho phép tìm kiếm sản phẩm theo tên, mã, danh mục.
 * Hiển thị:
 * 1. Thanh tìm kiếm
 * 2. Kết quả tìm kiếm (danh sách sản phẩm)
 * 3. Empty state khi không có kết quả
 */
@Composable
fun SearchScreen(
    filteredProducts: List<Product>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onAdjustStock: (Product, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Thanh tìm kiếm
        WarehouseSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        // Tiêu đề section: "Gợi ý tìm kiếm" hoặc "Kết quả tìm kiếm (X)"
        SectionHeader(
            title = if (searchQuery.isEmpty())
                stringResource(R.string.search_suggestions_header)
            else
                stringResource(
                    R.string.search_results_header,
                    filteredProducts.size
                ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        if (searchQuery.isEmpty()) {
            // Chưa nhập gì → hiển thị empty state với gợi ý
            EmptyState(
                icon = Icons.Rounded.Search,
                title = stringResource(R.string.search_empty_title),
                subtitle = stringResource(R.string.search_empty_subtitle)
            )
        } else if (filteredProducts.isEmpty()) {
            // Nhập rồi mà 0 kết quả
            EmptyState(
                icon = Icons.Rounded.SearchOff,
                title = stringResource(R.string.search_no_results_title),
                subtitle = stringResource(R.string.search_no_results_subtitle)
            )
        } else {
            // Hiển thị danh sách kết quả tìm kiếm
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = 16.dp, vertical = 8.dp
                ),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductListItem(
                        product = product,
                        onClick = { onProductClick(product) },
                        onEditClick = { onEditProduct(product) },
                        onAdjustStock = { amount ->
                            onAdjustStock(product, amount)
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }  // Padding cuối
            }
        }
    }
}
