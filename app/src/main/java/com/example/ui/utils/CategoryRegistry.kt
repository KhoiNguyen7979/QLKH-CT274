package com.example.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*

data class CategoryMeta(val name: String, val icon: ImageVector, val color: Color)

object CategoryRegistry {
    val categories = listOf(
        CategoryMeta("Thư mục", Icons.Rounded.Folder, CategoryFolders),
        CategoryMeta("Thời trang", Icons.Rounded.Checkroom, CategoryFashion),
        CategoryMeta("Công nghệ", Icons.Rounded.Devices, CategoryTech),
        CategoryMeta("Thực phẩm", Icons.Rounded.Restaurant, CategoryFood),
        CategoryMeta("Chung", Icons.Rounded.Category, CategoryGeneral)
    )

    fun getMeta(categoryName: String): CategoryMeta {
        return categories.find { it.name.equals(categoryName, ignoreCase = true) }
            ?: CategoryMeta(categoryName, Icons.Rounded.Category, CategoryGeneral)
    }
}
