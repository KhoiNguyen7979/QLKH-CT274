package com.example.ui.navigation

import android.app.Application
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.ProductViewModel
import com.example.ui.UiEvent
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Component Navigation chính của ứng dụng.
 * Thiết lập:
 * 1. NavController để quản lý điều hướng giữa các màn hình
 * 2. ProductViewModel (shared) để quản lý trạng thái toàn cục
 * 3. Theme (light/dark) dựa trên themeMode từ SharedPreferences
 * 4. SnackbarHost để hiển thị thông báo dạng snackbar
 * 5. NavHost với 4 routes chính
 */
@Composable
fun WarehouseNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Tạo ViewModel shared (tồn tại xuyên suốt vòng đời Navigation)
    val viewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(application)
    )

    val themeMode by viewModel.themeMode.collectAsState()

    // Xác định dark mode: 0=system, 1=light, 2=dark
    val isSystemDark = (context.resources.configuration.uiMode
        and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    val isDarkMode = when (themeMode) {
        1 -> false  // Luôn light
        2 -> true   // Luôn dark
        else -> isSystemDark  // Theo hệ thống
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Collect UI events (snackbar) từ ViewModel và hiển thị
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // Áp dụng theme và Scaffold với snackbar host
    MyApplicationTheme(darkTheme = isDarkMode) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // NavHost định nghĩa tất cả các routes và screen tương ứng
                NavHost(
                    navController = navController,
                    startDestination = "main"  // Màn hình mặc định khi mở app
                ) {
                    // ========== ROUTE: MÀN HÌNH CHÍNH (Tab Container) ==========
                    composable("main") {
                        MainTabsContainer(
                            viewModel = viewModel,
                            onProductClick = { product ->
                                navController.navigate("detail/${product.id}")
                            },
                            onEditProduct = { product ->
                                navController.navigate("add_edit?productId=${product.id}")
                            },
                            onAddProductClick = {
                                navController.navigate("add_edit")
                            },
                            onNotificationClick = { notification ->
                                navController.navigate("notification_detail/${notification.id}")
                            }
                        )
                    }

                    // ========== ROUTE: CHI TIẾT THÔNG BÁO ==========
                    // Truyền notificationId qua argument
                    composable(
                        route = "notification_detail/{notificationId}",
                        arguments = listOf(
                            navArgument("notificationId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val notificationId = backStackEntry.arguments?.getInt("notificationId") ?: 0
                        val notifications by viewModel.notifications.collectAsState()
                        val notification = notifications.find { it.id == notificationId }

                        // Tự động đánh dấu đã đọc khi mở chi tiết
                        LaunchedEffect(notificationId) {
                            viewModel.markNotificationAsRead(notificationId)
                        }

                        NotificationDetailScreen(
                            notification = notification,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // ========== ROUTE: CHI TIẾT SẢN PHẨM ==========
                    // Truyền productId qua argument
                    composable(
                        route = "detail/{productId}",
                        arguments = listOf(navArgument("productId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                        val products by viewModel.products.collectAsState()
                        val product = products.find { it.id == productId }
                        // Ghi nhận số lượng ban đầu khi vào màn (để flush notification khi rời đi)
                        LaunchedEffect(Unit) { product?.let { viewModel.setStockAdjustOrigin(it) } }

                        ProductDetailScreen(
                            product = product,
                            onBackClick = {
                                // Khi nhấn back → flush notification thay đổi tồn kho
                                product?.let { viewModel.flushStockAdjustNotification(it) }
                                navController.popBackStack()
                            },
                            onEditClick = {
                                navController.navigate("add_edit?productId=$productId")
                            },
                            onDeleteClick = {
                                if (product != null) {
                                    viewModel.deleteProduct(product)
                                }
                                navController.popBackStack()
                            },
                            onAdjustStock = { amount ->
                                product?.let { viewModel.adjustStock(it, amount) }
                            }
                        )
                    }

                    // ========== ROUTE: THÊM / CHỈNH SỬA SẢN PHẨM ==========
                    // productId nullable: có = edit mode, null = add mode
                    composable(
                        route = "add_edit?productId={productId}",
                        arguments = listOf(
                            navArgument("productId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val productIdStr = backStackEntry.arguments?.getString("productId")
                        val productId = productIdStr?.toIntOrNull()

                        val products by viewModel.products.collectAsState()
                        val productToEdit = if (productId != null) products.find { it.id == productId } else null

                        AddEditProductScreen(
                            product = productToEdit,
                            onBackClick = { navController.popBackStack() },
                            onSubmit = { name, code, qty, price, category, desc, imageUrls ->
                                if (productToEdit != null) {
                                    // Chế độ chỉnh sửa: tạo bản sao với thông tin mới
                                    val updatedProduct = productToEdit.copy(
                                        name = name,
                                        code = code,
                                        quantity = qty,
                                        price = price,
                                        category = category,
                                        description = desc,
                                        imageUrls = imageUrls,
                                        lastUpdated = System.currentTimeMillis()
                                    )
                                    viewModel.updateProduct(updatedProduct)
                                } else {
                                    // Chế độ thêm mới
                                    viewModel.addProduct(
                                        name = name,
                                        code = code,
                                        quantity = qty,
                                        price = price,
                                        category = category,
                                        description = desc,
                                        imageUrls = imageUrls
                                    )
                                }
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
