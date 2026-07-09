package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.Serializable

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
) : Serializable

class Converters {
    private val moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    fun fromString(value: String): List<String>? {
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return adapter.toJson(list)
    }
}
