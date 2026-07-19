package com.example.ui.navigation

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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

@Composable
fun WarehouseNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(application)
    )

    val isDarkMode by viewModel.isDarkMode.collectAsState()

    MyApplicationTheme(darkTheme = isDarkMode) {
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
                    onEditProduct = { product ->
                        navController.navigate("add_edit?productId=${product.id}")
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
                LaunchedEffect(Unit) { product?.let { viewModel.setStockAdjustOrigin(it) } }

                ProductDetailScreen(
                    product = product,
                    onBackClick = {
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
