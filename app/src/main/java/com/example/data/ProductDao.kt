package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
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

    @Query("SELECT * FROM warehouse_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<WarehouseNotification>>

    @Query("SELECT COUNT(*) FROM warehouse_notifications WHERE isRead = 0")
    fun getUnreadNotificationCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: WarehouseNotification)

    @Query("UPDATE warehouse_notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: Int)

    @Query("UPDATE warehouse_notifications SET isRead = 0 WHERE id = :notificationId")
    suspend fun markNotificationAsUnread(notificationId: Int)

    @Query("UPDATE warehouse_notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllNotificationsAsRead()

    @Delete
    suspend fun deleteNotification(notification: WarehouseNotification)

    @Query("DELETE FROM warehouse_notifications")
    suspend fun clearAllNotifications()
}
