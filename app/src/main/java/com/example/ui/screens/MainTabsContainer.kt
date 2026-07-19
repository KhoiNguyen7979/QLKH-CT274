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
import com.example.data.Product
import com.example.data.WarehouseNotification
import com.example.ui.ProductViewModel

@Composable
fun MainTabsContainer(
    viewModel: ProductViewModel,
    onProductClick: (Product) -> Unit,
    onEditProduct: (Product) -> Unit,
    onAddProductClick: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val products by viewModel.products.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            val tabTitle = when (currentTab) {
                0 -> "Tổng quan"
                1 -> "Kho hàng"
                2 -> "Thông báo"
                3 -> "Tìm kiếm"
                else -> "Kho Hàng"
            }
            WarehouseHeader(title = tabTitle, showActions = true, onActionClick = { showMenu = true })
        },
        bottomBar = { WarehouseBottomBar(currentTab = currentTab, unreadNotificationCount = unreadNotificationCount, onTabSelected = { viewModel.setTab(it) }) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentTab) {
                0 -> DashboardScreen(products = products, onProductClick = onProductClick, onNavigateToTab = { index -> viewModel.setTab(index) })
                1 -> ItemsListScreen(
                    products = products,
                    filteredProducts = filteredProducts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category -> viewModel.setSelectedCategory(category) },
                    onProductClick = onProductClick,
                    onEditProduct = onEditProduct,
                    onAddProductClick = onAddProductClick,
                    onAdjustStock = { product, amount -> viewModel.adjustStock(product, amount) }
                )
                2 -> NotificationsScreen(
                    notifications = notifications,
                    onClearAllClick = { viewModel.clearAllNotifications() },
                    onMarkAllAsReadClick = { viewModel.markAllNotificationsAsRead() },
                    onNotificationDismissed = { notification -> viewModel.deleteNotification(notification) },
                    onNotificationClick = { notification -> viewModel.markNotificationAsRead(notification.id) }
                )
                3 -> SearchScreen(
                    filteredProducts = filteredProducts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                    onProductClick = onProductClick,
                    onEditProduct = onEditProduct,
                    onAdjustStock = { product, amount -> viewModel.adjustStock(product, amount) }
                )
            }

            WarehouseMenuSheet(
                visible = showMenu,
                onDismiss = { showMenu = false },
                onAddProductClick = onAddProductClick,
                onSearchItemsClick = { viewModel.setTab(3) },
                onClearNotificationsClick = { viewModel.clearAllNotifications() },
                onAddRandomItemsClick = {
                    viewModel.addProduct(
                        name = "Sản phẩm ngẫu nhiên X",
                        code = "RD" + kotlin.random.Random.nextInt(100000, 999999),
                        quantity = kotlin.random.Random.nextInt(1, 50),
                        price = kotlin.random.Random.nextInt(10, 500).toDouble(),
                        category = listOf("Thời trang", "Công nghệ", "Thực phẩm", "Chung").random(),
                        description = "Sản phẩm tạo tự động nhằm kiểm tra chức năng phản hồi dữ liệu trong thời gian thực.",
                        imageUrls = listOf("preset_general")
                    )
                },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.toggleDarkMode() }
            )
        }
    }
}
