package com.example.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allNotifications: Flow<List<WarehouseNotification>> = productDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = productDao.getUnreadNotificationCount()

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    suspend fun getProductById(id: Int): Product? = productDao.getProductById(id)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun insertNotification(notification: WarehouseNotification) =
        productDao.insertNotification(notification)

    suspend fun markNotificationAsRead(notificationId: Int) =
        productDao.markNotificationAsRead(notificationId)

    suspend fun markNotificationAsUnread(notificationId: Int) =
        productDao.markNotificationAsUnread(notificationId)

    suspend fun markAllNotificationsAsRead() =
        productDao.markAllNotificationsAsRead()

    suspend fun deleteNotification(notification: WarehouseNotification) =
        productDao.deleteNotification(notification)

    suspend fun clearNotifications() = productDao.clearAllNotifications()
}
