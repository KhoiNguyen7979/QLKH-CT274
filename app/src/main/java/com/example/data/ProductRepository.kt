package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository - Lớp trung gian giữa Data Source (DAO) và ViewModel.
 * Encapsulate logic truy cập dữ liệu, cung cấp API sạch cho ViewModel.
 * Quản lý cả ProductDao và NotificationDao.
 */
class ProductRepository(
    private val productDao: ProductDao,
    private val notificationDao: NotificationDao
) {
    /** Flow danh sách tất cả sản phẩm (tự cập nhật khi DB thay đổi) */
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    /** Flow danh sách tất cả thông báo */
    val allNotifications: Flow<List<WarehouseNotification>> =
        notificationDao.getAllNotifications()

    /** Flow số lượng thông báo chưa đọc */
    val unreadNotificationCount: Flow<Int> =
        notificationDao.getUnreadNotificationCount()

    /** Tìm kiếm sản phẩm theo query */
    fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query)

    /** Lấy sản phẩm theo ID */
    suspend fun getProductById(id: Int): Product? =
        productDao.getProductById(id)

    /** Thêm sản phẩm mới */
    suspend fun insertProduct(product: Product): Long =
        productDao.insertProduct(product)

    /** Cập nhật sản phẩm */
    suspend fun updateProduct(product: Product) =
        productDao.updateProduct(product)

    /** Xóa sản phẩm */
    suspend fun deleteProduct(product: Product) =
        productDao.deleteProduct(product)

    /** Thêm thông báo mới */
    suspend fun insertNotification(
        notification: WarehouseNotification
    ) = notificationDao.insertNotification(notification)

    /** Đánh dấu thông báo đã đọc */
    suspend fun markNotificationAsRead(notificationId: Int) =
        notificationDao.markNotificationAsRead(notificationId)

    /** Đánh dấu thông báo chưa đọc */
    suspend fun markNotificationAsUnread(notificationId: Int) =
        notificationDao.markNotificationAsUnread(notificationId)

    /** Đánh dấu tất cả thông báo đã đọc */
    suspend fun markAllNotificationsAsRead() =
        notificationDao.markAllNotificationsAsRead()

    /** Xóa thông báo */
    suspend fun deleteNotification(
        notification: WarehouseNotification
    ) = notificationDao.deleteNotification(notification)

    /** Xóa tất cả thông báo */
    suspend fun clearNotifications() =
        notificationDao.clearAllNotifications()
}
