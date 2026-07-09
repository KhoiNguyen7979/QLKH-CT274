package com.example.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allNotifications: Flow<List<WarehouseNotification>> = productDao.getAllNotifications()

    fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query)
    }

    suspend fun getProductById(id: Int): Product? {
        return productDao.getProductById(id)
    }

    suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun insertNotification(notification: WarehouseNotification) {
        productDao.insertNotification(notification)
    }

    suspend fun clearNotifications() {
        productDao.clearAllNotifications()
    }
}
