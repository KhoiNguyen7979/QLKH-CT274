package com.example.ui.utils

import com.example.data.Product
import com.example.ui.screens.LOW_STOCK_THRESHOLD

fun Product.isLowStock(): Boolean = quantity <= LOW_STOCK_THRESHOLD
