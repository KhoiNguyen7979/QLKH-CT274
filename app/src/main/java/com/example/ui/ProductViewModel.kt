package com.example.ui

import android.app.Application
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.data.WarehouseDatabase
import com.example.data.WarehouseNotification
import com.example.ui.screens.LOW_STOCK_THRESHOLD
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("warehouse_prefs", 0)

    private fun getString(resId: Int) = getApplication<Application>().getString(resId)
    private fun getString(resId: Int, vararg args: Any) = getApplication<Application>().getString(resId, *args)

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 0))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
        prefs.edit { putInt("theme_mode", mode) }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val notifications: StateFlow<List<WarehouseNotification>> = repository.allNotifications
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val unreadNotificationCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val filteredProducts: StateFlow<List<Product>> = combine(
        products, _searchQuery, _selectedCategory
    ) { productsList, query, category ->
        productsList.filter { product ->
            val matchesSearch = query.isEmpty() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.code.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)
            val matchesCategory = category == "Tất cả" || product.category == category
            matchesSearch && matchesCategory
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    init {
        viewModelScope.launch {
            repository.allProducts.first().let { existingProducts ->
                if (existingProducts.isEmpty()) seedSampleData()
            }
        }
    }

    fun setTab(index: Int) { _currentTab.value = index }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSelectedCategory(category: String) { _selectedCategory.value = category }

    private suspend fun seedSampleData() {
        val samples = listOf(
            Product(
                name = "Nà ná na nà",
                code = "TH3636363636",
                quantity = 36,
                price = 1_010_000.0,
                category = "Folders",
                description = "Sản phẩm đặc biệt từ thiết kế gốc của người dùng.",
                imageUrls = listOf("preset_user_item")
            ),
            Product(
                name = "Áo Thun Polo Premium",
                code = "AP882910",
                quantity = 150,
                price = 250_000.0,
                category = "Thời trang",
                description = "Áo thun polo chất liệu cotton co giãn cao cấp.",
                imageUrls = listOf("preset_fashion")
            ),
            Product(
                name = "Tai Nghe Bluetooth Pro",
                code = "TN991823",
                quantity = 8,
                price = 850_000.0,
                category = "Công nghệ",
                description = "Tai nghe không dây chống ồn chủ động ANC.",
                imageUrls = listOf("preset_tech")
            ),
            Product(
                name = "Bàn Phím Cơ Silent",
                code = "BP441122",
                quantity = 25,
                price = 1_100_000.0,
                category = "Công nghệ",
                description = "Bàn phím cơ full-size sử dụng switch silent.",
                imageUrls = listOf("preset_tech2")
            ),
            Product(
                name = "Giày Sneaker Run X",
                code = "GS124509",
                quantity = 42,
                price = 650_000.0,
                category = "Thời trang",
                description = "Giày chạy bộ chuyên nghiệp siêu nhẹ.",
                imageUrls = listOf("preset_sneaker")
            )
        )
        samples.forEach { insertProductSafe(it) }

        insertNotificationSafe(
            WarehouseNotification(
                title = getString(R.string.notification_title_system_init),
                message = getString(R.string.notification_msg_system_init),
                type = "success"
            )
        )
        insertNotificationSafe(
            WarehouseNotification(
                title = getString(R.string.notification_title_low_stock),
                message = getString(R.string.notification_msg_sample_low_stock),
                type = "warning"
            )
        )
    }

    fun addProduct(
        name: String,
        code: String,
        quantity: Int,
        price: Double,
        category: String,
        description: String,
        imageUrls: List<String>
    ) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                code = code,
                quantity = quantity,
                price = price,
                category = category.ifEmpty { "Chung" },
                description = description,
                imageUrls = imageUrls
            )
            insertProductSafe(product)
            insertNotificationSafe(
                WarehouseNotification(
                    title = getString(R.string.notification_title_add_product),
                    message = getString(R.string.notification_msg_add_product, name, code),
                    type = "success"
                )
            )
            if (quantity <= LOW_STOCK_THRESHOLD) {
                insertNotificationSafe(
                    WarehouseNotification(
                        title = getString(R.string.notification_title_low_stock),
                        message = getString(R.string.notification_msg_new_low_stock, name, quantity),
                        type = "warning"
                    )
                )
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            val oldProduct = repository.allProducts.first().find { it.id == product.id }
            updateProductSafe(product)
            val nameChanged = oldProduct != null && oldProduct.name != product.name
            val codeChanged = oldProduct != null && oldProduct.code != product.code
            val categoryChanged = oldProduct != null && oldProduct.category != product.category
            val descriptionChanged = oldProduct != null && oldProduct.description != product.description
            val priceChanged = oldProduct != null && oldProduct.price != product.price
            val imagesChanged = oldProduct != null && oldProduct.imageUrls != product.imageUrls
            val quantityChanged = oldProduct != null && oldProduct.quantity != product.quantity
            if (nameChanged || codeChanged || categoryChanged ||
                descriptionChanged || priceChanged || imagesChanged
            ) {
                insertNotificationSafe(
                    WarehouseNotification(
                        title = getString(R.string.notification_title_update_product),
                        message = getString(
                            R.string.notification_msg_update_product,
                            product.name,
                            product.code
                        ),
                        type = "info"
                    )
                )
            }
            if (quantityChanged &&
                product.quantity <= LOW_STOCK_THRESHOLD &&
                oldProduct.quantity > LOW_STOCK_THRESHOLD
            ) {
                insertNotificationSafe(
                    WarehouseNotification(
                        title = getString(R.string.notification_title_low_stock),
                        message = getString(
                            R.string.notification_msg_update_low_stock,
                            product.name,
                            product.quantity
                        ),
                        type = "warning"
                    )
                )
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            deleteProductSafe(product)
            insertNotificationSafe(
                WarehouseNotification(
                    title = getString(R.string.notification_title_delete_product),
                    message = getString(
                        R.string.notification_msg_delete_product,
                        product.name,
                        product.code
                    ),
                    type = "warning"
                )
            )
        }
    }

    private var stockAdjustOriginalQty: Int? = null

    fun setStockAdjustOrigin(product: Product) {
        if (stockAdjustOriginalQty == null) stockAdjustOriginalQty = product.quantity
    }

    fun adjustStock(product: Product, amount: Int) {
        viewModelScope.launch {
            val newQty = (product.quantity + amount).coerceAtLeast(0)
            if (newQty == product.quantity) return@launch
            val updatedProduct = product.copy(quantity = newQty, lastUpdated = System.currentTimeMillis())
            updateProductSafe(updatedProduct)
        }
    }

    fun flushStockAdjustNotification(product: Product) {
        val originalQty = stockAdjustOriginalQty ?: return
        stockAdjustOriginalQty = null
        val currentQty = products.value.find { it.id == product.id }?.quantity ?: return
        if (originalQty == currentQty) return
        val actionType = if (currentQty > originalQty) {
            getString(R.string.stock_action_increase)
        } else {
            getString(R.string.stock_action_decrease)
        }
        val typeStr = if (currentQty > originalQty) "success" else "info"
        viewModelScope.launch {
            insertNotificationSafe(
                WarehouseNotification(
                    title = getString(R.string.notification_title_adjust_stock),
                    message = getString(
                        R.string.notification_msg_adjust_stock,
                        actionType,
                        product.name,
                        originalQty,
                        currentQty
                    ),
                    type = typeStr
                )
            )
            if (currentQty <= LOW_STOCK_THRESHOLD && originalQty > LOW_STOCK_THRESHOLD) {
                insertNotificationSafe(
                    WarehouseNotification(
                        title = getString(R.string.notification_title_low_stock),
                        message = getString(
                            R.string.notification_msg_adjust_low_stock,
                            product.name,
                            currentQty
                        ),
                        type = "warning"
                    )
                )
            }
        }
    }

    fun markNotificationAsRead(notificationId: Int) {
        viewModelScope.launch { markNotificationAsReadSafe(notificationId) }
    }

    fun markNotificationAsUnread(notificationId: Int) {
        viewModelScope.launch { markNotificationAsUnreadSafe(notificationId) }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch { markAllNotificationsAsReadSafe() }
    }

    fun deleteNotification(notification: WarehouseNotification) {
        viewModelScope.launch { deleteNotificationSafe(notification) }
    }

    fun clearAllNotifications() {
        viewModelScope.launch { clearNotificationsSafe() }
    }

    private suspend fun insertProductSafe(product: Product) {
        try {
            repository.insertProduct(product)
        } catch (e: Exception) {
            Log.e("ProductVM", "Insert product failed", e)
        }
    }

    private suspend fun updateProductSafe(product: Product) {
        try {
            repository.updateProduct(product)
        } catch (e: Exception) {
            Log.e("ProductVM", "Update product failed", e)
        }
    }

    private suspend fun deleteProductSafe(product: Product) {
        try {
            repository.deleteProduct(product)
        } catch (e: Exception) {
            Log.e("ProductVM", "Delete product failed", e)
        }
    }

    private suspend fun insertNotificationSafe(notification: WarehouseNotification) {
        try {
            repository.insertNotification(notification)
        } catch (e: Exception) {
            Log.e("ProductVM", "Insert notification failed", e)
        }
    }

    private suspend fun markNotificationAsReadSafe(id: Int) {
        try {
            repository.markNotificationAsRead(id)
        } catch (e: Exception) {
            Log.e("ProductVM", "Mark read failed", e)
        }
    }

    private suspend fun markNotificationAsUnreadSafe(id: Int) {
        try {
            repository.markNotificationAsUnread(id)
        } catch (e: Exception) {
            Log.e("ProductVM", "Mark unread failed", e)
        }
    }

    private suspend fun markAllNotificationsAsReadSafe() {
        try {
            repository.markAllNotificationsAsRead()
        } catch (e: Exception) {
            Log.e("ProductVM", "Mark all read failed", e)
        }
    }

    private suspend fun deleteNotificationSafe(notification: WarehouseNotification) {
        try {
            repository.deleteNotification(notification)
        } catch (e: Exception) {
            Log.e("ProductVM", "Delete notification failed", e)
        }
    }

    private suspend fun clearNotificationsSafe() {
        try {
            repository.clearNotifications()
        } catch (e: Exception) {
            Log.e("ProductVM", "Clear notifications failed", e)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                val database = WarehouseDatabase.getDatabase(application)
                val repository = ProductRepository(database.productDao(), database.notificationDao())
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
