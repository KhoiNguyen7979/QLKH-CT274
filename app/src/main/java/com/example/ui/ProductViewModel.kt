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

/**
 * sealed class định nghĩa các sự kiện UI một chiều (one-way events).
 * Dùng để gửi thông báo từ ViewModel lên UI (ví dụ: hiển thị snackbar).
 */
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}

/**
 * ViewModel chính của ứng dụng, quản lý toàn bộ trạng thái và logic nghiệp vụ.
 * Kế thừa AndroidViewModel để truy cập Application context (SharedPreferences, resources).
 * Không sử dụng DI framework (Hilt/Dagger) mà tự tạo Factory thủ công.
 */
class ProductViewModel(
    application: Application,
    private val repository: ProductRepository
) : AndroidViewModel(application) {

    /** SharedPreferences để lưu theme mode (0=system, 1=light, 2=dark) */
    private val prefs = application.getSharedPreferences("warehouse_prefs", 0)

    /** Helper lấy string từ resources theo resId */
    private fun getString(resId: Int) = getApplication<Application>().getString(resId)
    private fun getString(resId: Int, vararg args: Any) = getApplication<Application>().getString(resId, *args)

    // ==================== TRẠNG THÁI UI ====================

    /** Tab hiện tại (0=Dashboard, 1=Inventory, 2=Notifications, 3=Search) */
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    /** Channel gửi UI events (snackbar) từ ViewModel lên UI */
    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    /** Chế độ theme (0=system, 1=light, 2=dark), lưu trong SharedPreferences */
    private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 0))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    /** Đặt theme mode và lưu vào SharedPreferences */
    fun setThemeMode(mode: Int) {
        _themeMode.value = mode
        prefs.edit { putInt("theme_mode", mode) }
    }

    /** Query tìm kiếm hiện tại */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Danh mục được chọn để lọc (mặc định: "Tất cả") */
    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // ==================== DỮ LIỆU TỪ DATABASE ====================

    /** Flow danh sách tất cả sản phẩm từ Room DB */
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    /** Flow danh sách tất cả thông báo từ Room DB */
    val notifications: StateFlow<List<WarehouseNotification>> = repository.allNotifications
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    /** Flow số lượng thông báo chưa đọc */
    val unreadNotificationCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    /**
     * Flow danh sách sản phẩm đã lọc (kết hợp search query + danh mục).
     * Sử dụng combine() để tự động cập nhật khi searchQuery hoặc selectedCategory thay đổi.
     * Lọc theo: tên chứa query, mã chứa query, hoặc danh mục chứa query.
     */
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

    // ==================== KHỞI TẠO ====================

    init {
        // Khi ViewModel khởi tạo, kiểm tra DB trống thì seed dữ liệu mẫu
        viewModelScope.launch {
            repository.allProducts.first().let { existingProducts ->
                if (existingProducts.isEmpty()) seedSampleData()
            }
        }
    }

    /** Đặt tab hiện tại */
    fun setTab(index: Int) { _currentTab.value = index }
    /** Đặt query tìm kiếm */
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    /** Đặt danh mục được chọn */
    fun setSelectedCategory(category: String) { _selectedCategory.value = category }

    /**
     * Tạo dữ liệu mẫu khi DB trống (lần chạy đầu tiên).
     * Bao gồm 5 sản phẩm mẫu và 2 thông báo mẫu.
     */
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

        // Tạo thông báo mẫu: hệ thống khởi động
        insertNotificationSafe(
            WarehouseNotification(
                title = getString(R.string.notification_title_system_init),
                message = getString(R.string.notification_msg_system_init),
                type = "success"
            )
        )
        // Tạo thông báo mẫu: cảnh báo tồn kho thấp
        insertNotificationSafe(
            WarehouseNotification(
                title = getString(R.string.notification_title_low_stock),
                message = getString(R.string.notification_msg_sample_low_stock),
                type = "warning"
            )
        )
    }

    // ==================== CRUD SẢN PHẨM ====================

    /**
     * Thêm sản phẩm mới.
     * Tạo notification "thêm thành công" + snackbar.
     * Nếu số lượng ≤ 10 → thêm notification cảnh báo tồn kho thấp.
     */
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
            // Tạo thông báo thành công
            insertNotificationSafe(
                WarehouseNotification(
                    title = getString(R.string.notification_title_add_product),
                    message = getString(R.string.notification_msg_add_product, name, code),
                    type = "success"
                )
            )
            _uiEvents.tryEmit(UiEvent.ShowSnackbar(getString(R.string.snackbar_product_added)))
            // Cảnh báo nếu tồn kho thấp
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

    /**
     * Cập nhật sản phẩm.
     * So sánh field cũ vs mới → chỉ tạo notification nếu có thay đổi.
     * Nếu số lượng rớt xuống ≤ 10 → thêm notification low-stock warning.
     */
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            val oldProduct = repository.allProducts.first().find { it.id == product.id }
            updateProductSafe(product)
            // Kiểm tra từng field có thay đổi không
            val nameChanged = oldProduct != null && oldProduct.name != product.name
            val codeChanged = oldProduct != null && oldProduct.code != product.code
            val categoryChanged = oldProduct != null && oldProduct.category != product.category
            val descriptionChanged = oldProduct != null && oldProduct.description != product.description
            val priceChanged = oldProduct != null && oldProduct.price != product.price
            val imagesChanged = oldProduct != null && oldProduct.imageUrls != product.imageUrls
            val quantityChanged = oldProduct != null && oldProduct.quantity != product.quantity
            // Chỉ thông báo nếu có thay đổi về thông tin (không tính số lượng)
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
                _uiEvents.tryEmit(UiEvent.ShowSnackbar(getString(R.string.snackbar_product_updated)))
            }
            // Cảnh báo low-stock nếu số lượng rớt xuống ≤ 10
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

    /**
     * Xóa sản phẩm.
     * Tạo notification "đã xóa" + hiển thị snackbar.
     */
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
            _uiEvents.tryEmit(UiEvent.ShowSnackbar(getString(R.string.snackbar_product_deleted)))
        }
    }

    // ==================== ĐIỀU CHỈNH TỒN KHO ====================

    /** Lưu số lượng ban đầu khi vào màn Detail (để so sánh khi rời đi) */
    private var stockAdjustOriginalQty: Int? = null

    /**
     * Đặt số lượng ban đầu khi người dùng vào màn chi tiết sản phẩm.
     * Chỉ ghi nhận lần đầu tiên (nếu chưa có).
     */
    fun setStockAdjustOrigin(product: Product) {
        if (stockAdjustOriginalQty == null) stockAdjustOriginalQty = product.quantity
    }

    /**
     * Điều chỉnh số lượng tồn kho (+/-).
     * KHÔNG tạo notification ngay → chỉ flush khi rời màn Detail.
     * Số lượng mới không được nhỏ hơn 0.
     */
    fun adjustStock(product: Product, amount: Int) {
        viewModelScope.launch {
            val newQty = (product.quantity + amount).coerceAtLeast(0)
            if (newQty == product.quantity) return@launch
            val updatedProduct = product.copy(quantity = newQty, lastUpdated = System.currentTimeMillis())
            updateProductSafe(updatedProduct)
        }
    }

    /**
     * Tạo notification ghi nhận tổng thay đổi tồn kho khi rời màn Detail.
     * So sánh số lượng hiện tại vs số lượng ban đầu (khi vào màn).
     * Nếu rớt xuống ≤ 10 → thêm notification low-stock warning.
     */
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
            // Cảnh báo low-stock nếu rớt xuống ≤ 10
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

    // ==================== QUẢN LÝ THÔNG BÁO ====================

    /** Đánh dấu thông báo đã đọc */
    fun markNotificationAsRead(notificationId: Int) {
        viewModelScope.launch { markNotificationAsReadSafe(notificationId) }
    }

    /** Đánh dấu thông báo chưa đọc */
    fun markNotificationAsUnread(notificationId: Int) {
        viewModelScope.launch { markNotificationAsUnreadSafe(notificationId) }
    }

    /** Đánh dấu tất cả thông báo đã đọc */
    fun markAllNotificationsAsRead() {
        viewModelScope.launch { markAllNotificationsAsReadSafe() }
    }

    /** Xóa một thông báo */
    fun deleteNotification(notification: WarehouseNotification) {
        viewModelScope.launch { deleteNotificationSafe(notification) }
    }

    /** Xóa tất cả thông báo */
    fun clearAllNotifications() {
        viewModelScope.launch { clearNotificationsSafe() }
    }

    // ==================== HELPER FUNCTIONS (SAFE DB OPERATIONS) ====================
    // Các hàm này bọc try-catch để tránh crash khi có lỗi DB

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

    /**
     * Factory để tạo ProductViewModel thủ công (không dùng DI).
     * Tạo Database → Repository → ViewModel theo thứ tự.
     */
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
