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

data class BottomNavItem(
    val index: Int,
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val showBadge: Boolean = false
)

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
        showBadge = true
    ),
    BottomNavItem(
        3, R.string.tab_search,
        Icons.Rounded.Search, Icons.Rounded.Search
    )
)

@Composable
fun WarehouseBottomBar(
    currentTab: Int,
    unreadNotificationCount: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
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

                val iconColor = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant

                val textColor = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant

                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isSelected)
                                Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            else
                                Modifier
                        )
                        .clickable { onTabSelected(item.index) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.showBadge && unreadNotificationCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = StockDanger) {
                                    Text(
                                        text = if (unreadNotificationCount > 99)
                                            "99+"
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
