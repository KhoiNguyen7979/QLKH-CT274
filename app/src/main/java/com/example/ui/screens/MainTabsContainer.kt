package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.Product
import com.example.data.WarehouseNotification
import com.example.ui.ProductViewModel
import com.example.ui.components.*

/**
 * Container chính chứa 4 tab: Dashboard, Inventory, Notifications, Search.
 * Scaffold với top bar (header) và bottom bar (navigation).
 * Quản lý menu sheet (overflow menu) và chuyển tab.
 */
@Composable
fun MainTabsContainer(
    viewModel: ProductViewModel,
    onProductClick: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onAddProductClick: () -> Unit,
    onNotificationClick: (WarehouseNotification) -> Unit
) {
    // Thu thập trạng thái từ ViewModel
    val currentTab by viewModel.currentTab.collectAsState()
    val products by viewModel.products.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    // Trạng thái hiển thị menu sheet
    var showMenu by remember { mutableStateOf(false) }

    val randomProductName = stringResource(R.string.random_product_name)
    val randomProductDesc = stringResource(R.string.random_product_desc)

    Scaffold(
        topBar = {
            // Xác định tiêu đề theo tab hiện tại
            val tabTitle = when (currentTab) {
                0 -> stringResource(R.string.tab_dashboard)
                1 -> stringResource(R.string.tab_inventory)
                2 -> stringResource(R.string.tab_notifications)
                3 -> stringResource(R.string.tab_search)
                else -> stringResource(R.string.tab_inventory)
            }
            WarehouseHeader(
                title = tabTitle,
                showActions = true,
                onActionClick = { showMenu = true }  // Mở menu sheet
            )
        },
        bottomBar = {
            WarehouseBottomBar(
                currentTab = currentTab,
                unreadNotificationCount = unreadNotificationCount,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Chuyển đổi nội dung theo tab hiện tại
            when (currentTab) {
                0 -> DashboardScreen(
                    products = products,
                    onProductClick = onProductClick,
                    onNavigateToTab = { index -> viewModel.setTab(index) }
                )
                1 -> ItemsListScreen(
                    products = products,
                    filteredProducts = filteredProducts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query ->
                        viewModel.setSearchQuery(query)
                    },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        viewModel.setSelectedCategory(category)
                    },
                    onProductClick = onProductClick,
                    onEditProduct = onEditProduct,
                    onAddProductClick = onAddProductClick,
                    onAdjustStock = { product, amount ->
                        viewModel.adjustStock(product, amount)
                    }
                )
                2 -> NotificationsScreen(
                    notifications = notifications,
                    onClearAllClick = {
                        viewModel.clearAllNotifications()
                    },
                    onMarkAllAsReadClick = {
                        viewModel.markAllNotificationsAsRead()
                    },
                    onNotificationDismissed = { notification ->
                        viewModel.deleteNotification(notification)
                    },
                    onNotificationClick = onNotificationClick
                )
                3 -> SearchScreen(
                    filteredProducts = filteredProducts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query ->
                        viewModel.setSearchQuery(query)
                    },
                    onProductClick = onProductClick,
                    onEditProduct = onEditProduct,
                    onAdjustStock = { product, amount ->
                        viewModel.adjustStock(product, amount)
                    }
                )
            }

            // Menu sheet (overflow menu) hiển thị đè lên nội dung
            WarehouseMenuSheet(
                visible = showMenu,
                onDismiss = { showMenu = false },
                onAddProductClick = onAddProductClick,
                onSearchItemsClick = { viewModel.setTab(3) },  // Chuyển sang tab Search
                onClearNotificationsClick = {
                    viewModel.clearAllNotifications()
                },
                onAddRandomItemsClick = {
                    // Thêm sản phẩm ngẫu nhiên với dữ liệu random
                    viewModel.addProduct(
                        name = randomProductName,
                        code = "RD" +
                            kotlin.random.Random.nextInt(100000, 999999),
                        quantity = kotlin.random.Random.nextInt(1, 50),
                        price = kotlin.random.Random.nextInt(100_000, 5_000_000)
                            .toDouble(),
                        category = listOf(
                            "Thời trang", "Công nghệ",
                            "Thực phẩm", "Chung"
                        ).random(),
                        description = randomProductDesc,
                        imageUrls = listOf("preset_general")
                    )
                },
                themeMode = themeMode,
                onSetThemeMode = { viewModel.setThemeMode(it) }
            )
        }
    }
}
