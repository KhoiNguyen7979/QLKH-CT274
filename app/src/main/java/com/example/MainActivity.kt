package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.Product
import com.example.ui.ProductViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarehouseRed
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
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
    
    // Obtain our shared ViewModel
    val viewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(application)
    )

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        // Main Screen Route (Contains bottom navigation & tabs)
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

        // Product Detail Screen Route
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

        // Add/Edit Product Screen Route
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
                onSubmit = { name, code, qty, price, category, desc, imageUrl ->
                    if (isEditMode(productToEdit)) {
                        val updatedProduct = productToEdit!!.copy(
                            name = name,
                            code = code,
                            quantity = qty,
                            price = price,
                            category = category,
                            description = desc,
                            imageUrl = imageUrl,
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
                            imageUrl = imageUrl
                        )
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}

fun isEditMode(product: Product?): Boolean = product != null

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

    Scaffold(
        topBar = {
            // Header adapts its title dynamically based on the current active tab
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
                onActionClick = { showMenu = !showMenu }
            )
        },
        bottomBar = {
            // Custom bottom navigation bar respecting gesture pill safe areas
            NavigationBar(
                containerColor = Color.White,
                contentColor = WarehouseRed,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Tab 0: Dashboard
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

                // Tab 1: Items List
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

                // Tab 2: Notifications
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

                // Tab 3: Search
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
            // Render active tab body
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
                    onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category -> viewModel.setSelectedCategory(category) },
                    onProductClick = onProductClick,
                    onAddProductClick = onAddProductClick,
                    onAdjustStock = { product, amount -> viewModel.adjustStock(product, amount) }
                )
                2 -> NotificationsScreen(
                    notifications = notifications,
                    onClearAllClick = { viewModel.clearAllNotifications() }
                )
                3 -> SearchScreen(
                    filteredProducts = filteredProducts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                    onProductClick = onProductClick,
                    onAdjustStock = { product, amount -> viewModel.adjustStock(product, amount) }
                )
            }

            // Expanded dropdown choices on top bar
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Thêm sản phẩm mới") },
                    leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        onAddProductClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Làm sạch danh sách alerts") },
                    leadingIcon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        viewModel.clearAllNotifications()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Nạp thêm sản phẩm mẫu") },
                    leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                    onClick = {
                        showMenu = false
                        viewModel.addProduct(
                            name = "Sản phẩm ngẫu nhiên X",
                            code = "RD" + Random.nextInt(100000, 999999),
                            quantity = Random.nextInt(1, 50),
                            price = Random.nextInt(10, 500).toDouble(),
                            category = listOf("Thời trang", "Công nghệ", "Thực phẩm", "Chung").random(),
                            description = "Sản phẩm tạo tự động nhằm kiểm tra chức năng phản hồi dữ liệu trong thời gian thực.",
                            imageUrl = "preset_general"
                        )
                    }
                )
            }
        }
    }
}
