package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cho bảng warehouse_notifications.
 * Quản lý các thao tác CRUD cho thông báo.
 * Các phương thức trả về Flow để UI tự động cập nhật khi có thay đổi.
 */
@Dao
interface NotificationDao {
    /** Lấy tất cả thông báo, sắp xếp theo thời gian mới nhất */
    @Query("SELECT * FROM warehouse_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<WarehouseNotification>>

    /** Đếm số thông báo chưa đọc (dùng để hiển thị badge trên tab) */
    @Query("SELECT COUNT(*) FROM warehouse_notifications WHERE isRead = 0")
    fun getUnreadNotificationCount(): Flow<Int>

    /** Thêm thông báo mới */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: WarehouseNotification)

    /** Đánh dấu một thông báo đã đọc */
    @Query("UPDATE warehouse_notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: Int)

    /** Đánh dấu một thông báo chưa đọc (bỏ đánh dấu đã đọc) */
    @Query("UPDATE warehouse_notifications SET isRead = 0 WHERE id = :notificationId")
    suspend fun markNotificationAsUnread(notificationId: Int)

    /** Đánh dấu tất cả thông báo đã đọc */
    @Query("UPDATE warehouse_notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllNotificationsAsRead()

    /** Xóa một thông báo */
    @Delete
    suspend fun deleteNotification(notification: WarehouseNotification)

    /** Xóa tất cả thông báo */
    @Query("DELETE FROM warehouse_notifications")
    suspend fun clearAllNotifications()
}
