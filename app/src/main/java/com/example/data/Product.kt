package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val imageUrl: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) : Serializable
