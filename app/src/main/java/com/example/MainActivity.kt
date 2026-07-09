package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.Product
import com.example.data.WarehouseNotification
import com.example.ui.ProductViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarehouseRed
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(application)
    )

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainTabsContainer(
                viewModel = viewModel,
                onProductClick = { product ->
                    navController.navigate("detail/${product.id}")
                },
                onAddProductClick = {
                    navController.navigate("add_edit")
                }
            )
        }

        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            val products by viewModel.products.collectAsState()
            val product = products.find { it.id == productId }

            ProductDetailScreen(
                product = product,
                onBackClick = { navController.popBackStack() },
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
                    if (isEditMode(productToEdit)) {
                        val updatedProduct = productToEdit!!.copy(
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

fun isEditMode(product: Product?): Boolean = product != null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsContainer(
    viewModel: ProductViewModel,
    onProductClick: (Product) -> Unit,
    onAddProductClick: () -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val products by viewModel.products.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            val tabTitle = when (currentTab) {
                0 -> "Dashboard"
                1 -> "Items"
                2 -> "Notifications"
                3 -> "Search"
                else -> "Kho Hàng"
            }
            WarehouseHeader(
                title = tabTitle,
                showActions = true,
                onActionClick = { showMenu = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = WarehouseRed,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 0) Icons.Rounded.Dashboard else Icons.Rounded.DashboardCustomize,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarehouseRed,
                        selectedTextColor = WarehouseRed,
                        indicatorColor = WarehouseRed.copy(alpha = 0.15f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 1) Icons.Rounded.Inventory else Icons.Rounded.Inventory2,
                            contentDescription = "Items"
                        )
                    },
                    label = { Text("Items", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarehouseRed,
                        selectedTextColor = WarehouseRed,
                        indicatorColor = WarehouseRed.copy(alpha = 0.15f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = {
                        BadgedBox(
                            badge = {
                                val unreadCount = notifications.size
                                if (unreadCount > 0) {
                                    Badge(containerColor = WarehouseRed) {
                                        Text(unreadCount.toString(), color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == 2) Icons.Rounded.NotificationsActive else Icons.Rounded.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    },
                    label = { Text("Alerts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarehouseRed,
                        selectedTextColor = WarehouseRed,
                        indicatorColor = WarehouseRed.copy(alpha = 0.15f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search"
                        )
                    },
                    label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WarehouseRed,
                        selectedTextColor = WarehouseRed,
                        indicatorColor = WarehouseRed.copy(alpha = 0.15f),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    products = products,
                    onProductClick = onProductClick,
                    onNavigateToTab = { index: Int -> viewModel.setTab(index) }
                )
                1 -> ItemsListScreen(
                    products = products,
                    filteredProducts = filteredProducts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query: String -> viewModel.setSearchQuery(query) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category: String -> viewModel.setSelectedCategory(category) },
                    onProductClick = onProductClick,
                    onAddProductClick = onAddProductClick,
                    onAdjustStock = { product: Product, amount: Int -> viewModel.adjustStock(product, amount) }
                )
                // ĐÃ FIX TẠI ĐÂY: Ép kiểu dữ liệu rõ ràng cho tất cả tham số lambda đầu vào rộng rãi
                2 -> NotificationsScreen(
                    notifications = notifications,
                    onClearAllClick = { viewModel.clearAllNotifications() },
                    onNotificationDismissed = { _: WarehouseNotification -> },
                    onNotificationClick = { _: WarehouseNotification -> }
                )
                3 -> SearchScreen(
                    filteredProducts = filteredProducts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query: String -> viewModel.setSearchQuery(query) },
                    onProductClick = onProductClick,
                    onAdjustStock = { product: Product, amount: Int -> viewModel.adjustStock(product, amount) }
                )
            }

            if (showMenu) {
                ModalBottomSheet(
                    onDismissRequest = { showMenu = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        WarehouseMenuButton(
                            text = "Add items",
                            icon = Icons.Rounded.Add,
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showMenu = false
                                }
                                onAddProductClick()
                            }
                        )

                        WarehouseMenuButton(
                            text = "Search items",
                            icon = Icons.Rounded.Search,
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showMenu = false
                                }
                                viewModel.setTab(3)
                            }
                        )

                        WarehouseMenuButton(
                            text = "Clear notifications",
                            icon = Icons.Rounded.DeleteForever,
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showMenu = false
                                }
                                viewModel.clearAllNotifications()
                            }
                        )

                        WarehouseMenuButton(
                            text = "Add random items",
                            icon = Icons.Rounded.Refresh,
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showMenu = false
                                }
                                viewModel.addProduct(
                                    name = "Sản phẩm ngẫu nhiên X",
                                    code = "RD" + Random.nextInt(100000, 999999),
                                    quantity = Random.nextInt(1, 50),
                                    price = Random.nextInt(10, 500).toDouble(),
                                    category = listOf("Thời trang", "Công nghệ", "Thực phẩm", "Chung").random(),
                                    description = "Sản phẩm tạo tự động nhằm kiểm tra chức năng phản hồi dữ liệu trong thời gian thực.",
                                    imageUrls = listOf("preset_general")
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}