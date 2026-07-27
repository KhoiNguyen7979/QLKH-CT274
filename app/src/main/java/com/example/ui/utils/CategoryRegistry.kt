package com.example.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

/**
 * Data class chứa thông tin metadata của một danh mục.
 * - name: tên danh mục
 * - icon: Material icon
 * - color: màu đặc trưng
 */
data class CategoryMeta(val name: String, val icon: ImageVector, val color: Color)

/**
 * Registry chứa thông tin tất cả các danh mục trong ứng dụng.
 * Mỗi danh mục có: tên, icon, màu sắc.
 * Dùng để hiển thị icon + màu theo danh mục sản phẩm.
 */
object CategoryRegistry {
    val categories = listOf(
        CategoryMeta("Thư mục", Icons.Rounded.Folder, CategoryFolders),
        CategoryMeta("Thời trang", Icons.Rounded.Checkroom, CategoryFashion),
        CategoryMeta("Công nghệ", Icons.Rounded.Devices, CategoryTech),
        CategoryMeta("Thực phẩm", Icons.Rounded.Restaurant, CategoryFood),
        CategoryMeta("Chung", Icons.Rounded.Category, CategoryGeneral)
    )

    /**
     * Lấy metadata theo tên danh mục (case-insensitive).
     * Nếu không tìm thấy → trả về CategoryMeta mặc định (General).
     */
    fun getMeta(categoryName: String): CategoryMeta {
        return categories.find { it.name.equals(categoryName, ignoreCase = true) }
            ?: CategoryMeta(categoryName, Icons.Rounded.Category, CategoryGeneral)
    }
}
