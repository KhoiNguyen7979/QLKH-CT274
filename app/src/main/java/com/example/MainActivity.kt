package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.WarehouseNavigation

/**
 * Activity chính của ứng dụng QLKH (Quản lý Kho Hàng).
 * - enableEdgeToEdge(): hiển thị full screen (tràn vào status/nav bar)
 * - setContent: render WarehouseNavigation (NavHost + all screens)
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WarehouseNavigation()
        }
    }
}
