package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) cho bảng products.
 * Cung cấp các phương thức truy cập dữ liệu sản phẩm.
 * Các phương thức trả về Flow để cập nhật UI real-time khi dữ liệu thay đổi.
 */
@Dao
interface ProductDao {
    /** Lấy tất cả sản phẩm, sắp xếp theo thời gian cập nhật mới nhất */
    @Query("SELECT * FROM products ORDER BY lastUpdated DESC")
    fun getAllProducts(): Flow<List<Product>>

    /** Lấy sản phẩm theo ID (suspend function, gọi trong coroutine) */
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    /** Tìm kiếm sản phẩm theo tên, mã, hoặc danh mục (LIKE query) */
    @Query(
        "SELECT * FROM products " +
        "WHERE name LIKE '%' || :query || '%' " +
        "OR code LIKE '%' || :query || '%' " +
        "OR category LIKE '%' || :query || '%' " +
        "ORDER BY lastUpdated DESC"
    )
    fun searchProducts(query: String): Flow<List<Product>>

    /** Thêm sản phẩm mới. REPLACE strategy = nếu id trùng thì ghi đè */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    /** Cập nhật thông tin sản phẩm */
    @Update
    suspend fun updateProduct(product: Product)

    /** Xóa sản phẩm khỏi database */
    @Delete
    suspend fun deleteProduct(product: Product)
}
