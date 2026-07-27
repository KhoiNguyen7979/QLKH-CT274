package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Entity Room cho bảng "products" trong database.
 * Đại diện cho một mặt hàng trong kho với các thông tin:
 * - id: khóa chính, tự tăng
 * - name: tên sản phẩm
 * - code: mã SKU/định danh sản phẩm
 * - quantity: số lượng tồn kho
 * - price: đơn giá
 * - category: danh mục (Thời trang, Công nghệ, Thực phẩm, v.v.)
 * - description: mô tả chi tiết
 * - imageUrls: danh sách đường dẫn ảnh (tối đa 4 ảnh)
 * - lastUpdated: thời gian cập nhật cuối cùng (dùng để sắp xếp)
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String,
    val quantity: Int,
    val price: Double,
    val category: String = "Chung",
    val description: String = "",
    val imageUrls: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Class chuyển đổi kiểu dữ liệu cho Room Database.
 * Chuyển đổi giữa List<String> (danh sách URL ảnh) và String (JSON)
 * vì Room không hỗ trợ lưu List<String> trực tiếp.
 * Sử dụng Moshi library để serialize/deserialize JSON.
 */
class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    /** Chuyển JSON string → List<String> (danh sách URL ảnh) */
    @TypeConverter
    fun fromString(value: String): List<String>? {
        return adapter.fromJson(value)
    }

    /** Chuyển List<String> (danh sách URL ảnh) → JSON string để lưu vào DB */
    @TypeConverter
    fun fromList(list: List<String>): String {
        return adapter.toJson(list)
    }
}
