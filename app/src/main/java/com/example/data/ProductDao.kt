package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    // Product queries
    @Query("SELECT * FROM products ORDER BY lastUpdated DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY lastUpdated DESC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    // Notification queries
    @Query("SELECT * FROM warehouse_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<WarehouseNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: WarehouseNotification)

    @Query("DELETE FROM warehouse_notifications")
    suspend fun clearAllNotifications()
}
