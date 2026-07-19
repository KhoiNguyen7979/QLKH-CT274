package com.example.ui.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.StockDanger

data class BottomNavItem(
    val index: Int,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showBadge: Boolean = false
)

private val bottomNavItems = listOf(
    BottomNavItem(0, "Tổng quan", Icons.Rounded.Dashboard, Icons.Rounded.DashboardCustomize),
    BottomNavItem(1, "Kho hàng", Icons.Rounded.Inventory, Icons.Rounded.Folder),
    BottomNavItem(2, "Thông báo", Icons.Rounded.NotificationsActive, Icons.Rounded.Notifications, showBadge = true),
    BottomNavItem(3, "Tìm kiếm", Icons.Rounded.Search, Icons.Rounded.Search)
)

@Composable
fun WarehouseBottomBar(
    currentTab: Int,
    unreadNotificationCount: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentTab == item.index,
                onClick = { onTabSelected(item.index) },
                icon = {
                    if (item.showBadge && unreadNotificationCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = StockDanger) {
                                    Text(
                                        if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == item.index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (currentTab == item.index) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
