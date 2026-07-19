package com.example.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val notificationDao: NotificationDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allNotifications: Flow<List<WarehouseNotification>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadNotificationCount()

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)
    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun insertNotification(notification: WarehouseNotification) = notificationDao.insertNotification(notification)
    suspend fun markNotificationAsRead(notificationId: Int) = notificationDao.markNotificationAsRead(notificationId)
    suspend fun markNotificationAsUnread(notificationId: Int) = notificationDao.markNotificationAsUnread(notificationId)
    suspend fun markAllNotificationsAsRead() = notificationDao.markAllNotificationsAsRead()
    suspend fun deleteNotification(notification: WarehouseNotification) = notificationDao.deleteNotification(notification)
    suspend fun clearNotifications() = notificationDao.clearAllNotifications()
}
