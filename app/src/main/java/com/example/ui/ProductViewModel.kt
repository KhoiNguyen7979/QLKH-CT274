package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.data.WarehouseDatabase
import com.example.data.WarehouseNotification
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    // Active screen navigation tab index (0: Dashboard, 1: Items, 2: Notifications, 3: Search)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Search queries
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter category selection ("Tất cả" or a specific category)
    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // List of all products
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // List of notifications
    val notifications: StateFlow<List<WarehouseNotification>> = repository.allNotifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered products list based on search and selected category
    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        _searchQuery,
        _selectedCategory
    ) { productsList, query, category ->
        productsList.filter { product ->
            val matchesSearch = query.isEmpty() || 
                    product.name.contains(query, ignoreCase = true) || 
                    product.code.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)
            
            val matchesCategory = category == "Tất cả" || product.category == category
            
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Pre-populate with realistic and user-inspired sample data on first run
        viewModelScope.launch {
            repository.allProducts.first().let { existingProducts ->
                if (existingProducts.isEmpty()) {
                    seedSampleData()
                }
            }
        }
    }

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    private suspend fun seedSampleData() {
        // Sample products based on user screenshot and realistic items
        val samples = listOf(
            Product(
                name = "Nà ná na nà",
                code = "TH3636363636",
                quantity = 36,
                price = 1010.0,
                category = "Folders",
                description = "Sản phẩm đặc biệt từ thiết kế gốc của người dùng. Có giá trị lưu trữ cao và thuộc diện quản lý chặt chẽ trong phân khu A.",
                imageUrls = listOf("preset_user_item")
            ),
            Product(
                name = "Áo Thun Polo Premium",
                code = "AP882910",
                quantity = 150,
                price = 25.0,
                category = "Thời trang",
                description = "Áo thun polo chất liệu cotton co giãn cao cấp, thoáng mát, thích hợp mọi thời tiết.",
                imageUrls = listOf("preset_fashion")
            ),
            Product(
                name = "Tai Nghe Bluetooth Pro",
                code = "TN991823",
                quantity = 8, // Low stock on purpose
                price = 85.0,
                category = "Công nghệ",
                description = "Tai nghe không dây chống ồn chủ động ANC, âm bass cực mạnh, thời lượng pin sử dụng 30 tiếng liên tục.",
                imageUrls = listOf("preset_tech")
            ),
            Product(
                name = "Bàn Phím Cơ Silent",
                code = "BP441122",
                quantity = 25,
                price = 110.0,
                category = "Công nghệ",
                description = "Bàn phím cơ full-size sử dụng switch silent êm ái, thích hợp làm việc văn phòng và chơi game đêm khuya.",
                imageUrls = listOf("preset_tech2")
            ),
            Product(
                name = "Giày Sneaker Run X",
                code = "GS124509",
                quantity = 42,
                price = 65.0,
                category = "Thời trang",
                description = "Giày chạy bộ chuyên nghiệp siêu nhẹ, đệm lót êm ái hỗ trợ tối đa lực phản hồi khi di chuyển.",
                imageUrls = listOf("preset_sneaker")
            )
        )

        samples.forEach { repository.insertProduct(it) }

        // Feed some initial notifications
        repository.insertNotification(
            WarehouseNotification(
                title = "Hệ thống khởi tạo",
                message = "Đã nạp thành công dữ liệu mẫu của kho hàng.",
                type = "success"
            )
        )
        repository.insertNotification(
            WarehouseNotification(
                title = "Cảnh báo tồn kho thấp",
                message = "Sản phẩm 'Tai Nghe Bluetooth Pro' (TN991823) chỉ còn 8 chiếc trong kho!",
                type = "warning"
            )
        )
    }

    // Insert a new product
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
            repository.insertProduct(product)
            
            // Add notification
            repository.insertNotification(
                WarehouseNotification(
                    title = "Thêm sản phẩm mới",
                    message = "Đã thêm '$name' (Mã: $code) vào kho hàng thành công.",
                    type = "success"
                )
            )

            // If low stock upon addition
            if (quantity <= 10) {
                repository.insertNotification(
                    WarehouseNotification(
                        title = "Cảnh báo tồn kho thấp",
                        message = "Sản phẩm mới thêm '$name' có số lượng tồn kho thấp: $quantity sản phẩm.",
                        type = "warning"
                    )
                )
            }
        }
    }

    // Update existing product
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
            repository.insertNotification(
                WarehouseNotification(
                    title = "Cập nhật sản phẩm",
                    message = "Đã cập nhật thông tin sản phẩm '${product.name}' (Mã: ${product.code}).",
                    type = "info"
                )
            )

            // Dynamic low stock warning check
            if (product.quantity <= 10) {
                repository.insertNotification(
                    WarehouseNotification(
                        title = "Cảnh báo tồn kho thấp",
                        message = "Sản phẩm '${product.name}' hiện chỉ còn lại ${product.quantity} sản phẩm.",
                        type = "warning"
                    )
                )
            }
        }
    }

    // Delete product
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            repository.insertNotification(
                WarehouseNotification(
                    title = "Xóa sản phẩm",
                    message = "Đã xóa sản phẩm '${product.name}' (Mã: ${product.code}) khỏi kho hàng.",
                    type = "warning"
                )
            )
        }
    }

    // Update stock quantity directly (+ or -)
    fun adjustStock(product: Product, amount: Int) {
        viewModelScope.launch {
            val newQty = (product.quantity + amount).coerceAtLeast(0)
            if (newQty == product.quantity) return@launch

            val updatedProduct = product.copy(quantity = newQty, lastUpdated = System.currentTimeMillis())
            repository.updateProduct(updatedProduct)

            val actionType = if (amount > 0) "Tăng" else "Giảm"
            val typeStr = if (amount > 0) "success" else "info"
            
            repository.insertNotification(
                WarehouseNotification(
                    title = "Điều chỉnh tồn kho",
                    message = "$actionType số lượng '${product.name}' từ ${product.quantity} -> $newQty chiếc.",
                    type = typeStr
                )
            )

            // Check if triggers low stock warning
            if (newQty <= 10 && product.quantity > 10) {
                repository.insertNotification(
                    WarehouseNotification(
                        title = "Cảnh báo tồn kho thấp",
                        message = "Số lượng '${product.name}' giảm mạnh xuống mức cảnh báo: $newQty sản phẩm.",
                        type = "warning"
                    )
                )
            }
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    // Factory for constructing ViewModel with Repository
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                val database = WarehouseDatabase.getDatabase(application)
                val repository = ProductRepository(database.productDao())
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
