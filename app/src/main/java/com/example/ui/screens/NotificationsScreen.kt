package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.WarehouseNotification
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    notifications: List<WarehouseNotification>,
    onClearAllClick: () -> Unit,
    onMarkAllAsReadClick: () -> Unit,
    onNotificationDismissed: (WarehouseNotification) -> Unit,
    onNotificationClick: (WarehouseNotification) -> Unit
) {
    val now = System.currentTimeMillis()
    val oneDay = 24 * 60 * 60 * 1000L
    val unreadCount = notifications.count { !it.isRead }
    val newNotifications = notifications.filter { now - it.timestamp < oneDay }
    val earlierNotifications = notifications.filter { now - it.timestamp >= oneDay }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.notification_screen_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                unreadCount.toString(),
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
                if (notifications.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAllClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.btn_clear_all),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            if (unreadCount > 0) {
                TextButton(
                    onClick = onMarkAllAsReadClick,
                    contentPadding = PaddingValues(
                        horizontal = 0.dp, vertical = 4.dp
                    )
                ) {
                    Icon(Icons.Rounded.DoneAll, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.btn_mark_all_read),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (notifications.isNotEmpty()) {
                Text(
                    text = if (unreadCount > 0)
                        stringResource(
                            R.string.notifications_unread_summary,
                            unreadCount,
                            notifications.size
                        )
                    else
                        stringResource(R.string.notifications_all_read),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (notifications.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.NotificationsOff,
                title = stringResource(R.string.notifications_empty_title),
                subtitle = stringResource(R.string.notifications_empty_subtitle)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 16.dp, vertical = 4.dp
                )
            ) {
                if (newNotifications.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.section_newest),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(newNotifications, key = { it.id }) { notification ->
                        SwipeToDismissNotificationItem(
                            notification = notification,
                            onDismiss = {
                                onNotificationDismissed(notification)
                            }
                        ) {
                            NotificationItemRow(notification) {
                                onNotificationClick(notification)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
                if (earlierNotifications.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = stringResource(R.string.section_earlier),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(
                        earlierNotifications,
                        key = { it.id }
                    ) { notification ->
                        SwipeToDismissNotificationItem(
                            notification = notification,
                            onDismiss = {
                                onNotificationDismissed(notification)
                            }
                        ) {
                            NotificationItemRow(notification) {
                                onNotificationClick(notification)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
