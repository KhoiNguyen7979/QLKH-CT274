package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.StockDanger

/**
 * Data class định nghĩa một mục trong bottom navigation bar.
 * - index: vị trí tab
 * - labelResId: resource ID cho label
 * - selectedIcon: icon khi tab được chọn
 * - unselectedIcon: icon khi tab chưa chọn
 * - showBadge: có hiển thị badge (số thông báo chưa đọc) hay không
 */
data class BottomNavItem(
    val index: Int,
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showBadge: Boolean = false
)

/** Danh sách 4 tab trong bottom navigation */
private val bottomNavItems = listOf(
    BottomNavItem(
        0, R.string.tab_dashboard,
        Icons.Rounded.Dashboard, Icons.Rounded.DashboardCustomize
    ),
    BottomNavItem(
        1, R.string.tab_inventory,
        Icons.Rounded.Inventory, Icons.Rounded.Folder
    ),
    BottomNavItem(
        2, R.string.tab_notifications,
        Icons.Rounded.NotificationsActive,
        Icons.Rounded.Notifications,
        showBadge = true  // Tab thông báo có badge
    ),
    BottomNavItem(
        3, R.string.tab_search,
        Icons.Rounded.Search, Icons.Rounded.Search
    )
)

/**
 * Bottom Navigation Bar tùy chỉnh.
 * Hiển thị 4 tab với icon + label.
 * Tab Notifications hiển thị badge số thông báo chưa đọc.
 * Tab được chọn có background highlight.
 */
@Composable
fun WarehouseBottomBar(
    currentTab: Int,
    unreadNotificationCount: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)  // Tuân thủ navigation bar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bottomNavItems.forEach { item ->
                val label = stringResource(item.labelResId)
                val isSelected = currentTab == item.index

                // Màu icon/text: primary nếu chọn, onSurfaceVariant nếu không
                val iconColor = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant

                val textColor = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant

                // Icon thay đổi theo trạng thái chọn
                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            // Background highlight khi tab được chọn
                            if (isSelected)
                                Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            else
                                Modifier
                        )
                        .clickable { onTabSelected(item.index) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Hiển thị badge nếu có thông báo chưa đọc
                    if (item.showBadge && unreadNotificationCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = StockDanger) {
                                    Text(
                                        text = if (unreadNotificationCount > 99)
                                            "99+"  // Giới hạn hiển thị
                                        else
                                            unreadNotificationCount.toString(),
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor
                                )
                            }
                        }
                    } else {
                        // Không có badge
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
