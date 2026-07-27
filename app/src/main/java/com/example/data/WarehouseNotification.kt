package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room cho bảng "warehouse_notifications".
 * Đại diện cho một thông báo trong hệ thống kho hàng.
 * - id: khóa chính, tự tăng
 * - title: tiêu đề thông báo
 * - message: nội dung chi tiết thông báo
 * - timestamp: thời gian tạo thông báo (milliseconds)
 * - type: loại thông báo ("success", "warning", "info")
 * - isRead: đã đọc hay chưa (hiển thị badge đỏ nếu chưa đọc)
 */
@Entity(tableName = "warehouse_notifications")
data class WarehouseNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "info",
    val isRead: Boolean = false
)
