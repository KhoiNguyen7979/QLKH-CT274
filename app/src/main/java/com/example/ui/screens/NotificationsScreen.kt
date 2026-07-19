package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.data.WarehouseNotification
import com.example.ui.theme.*
import android.text.format.DateUtils

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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Thông báo", style = MaterialTheme.typography.headlineSmall)
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(unreadCount.toString(), color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAllClick, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Rounded.DeleteSweep, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Xóa tất cả", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (unreadCount > 0) {
                TextButton(onClick = onMarkAllAsReadClick, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
                    Icon(Icons.Rounded.DoneAll, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Đánh dấu tất cả đã đọc", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (notifications.isNotEmpty()) {
                Text(
                    text = if (unreadCount > 0) "$unreadCount thông báo chưa đọc trong tổng số ${notifications.size}" else "Tất cả thông báo đã được đọc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (notifications.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.NotificationsOff,
                title = "Bạn không có thông báo nào",
                subtitle = "Các thông báo về kho hàng sẽ xuất hiện ở đây"
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                if (newNotifications.isNotEmpty()) {
                    item { SectionHeader(title = "MỚI NHẤT", modifier = Modifier.padding(vertical = 8.dp)) }
                    items(newNotifications, key = { it.id }) { notification ->
                        SwipeToDismissNotificationItem(notification = notification, onDismiss = { onNotificationDismissed(notification) }) {
                            NotificationItemRow(notification) { if (!notification.isRead) onNotificationClick(notification) }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
                if (earlierNotifications.isNotEmpty()) {
                    item { SectionHeader(title = "TRƯỚC ĐÓ", modifier = Modifier.padding(vertical = 8.dp)) }
                    items(earlierNotifications, key = { it.id }) { notification ->
                        SwipeToDismissNotificationItem(notification = notification, onDismiss = { onNotificationDismissed(notification) }) {
                            NotificationItemRow(notification) { if (!notification.isRead) onNotificationClick(notification) }
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun NotificationItemRow(notification: WarehouseNotification, onClick: () -> Unit) {
    val (icon, tintColor) = when (notification.type) {
        "success" -> Icons.Rounded.CheckCircle to StockSafe
        "warning" -> Icons.Rounded.Warning to StockWarning
        "info" -> Icons.Rounded.Info to CategoryFolders
        else -> Icons.Rounded.Notifications to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(tintColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, notification.type, tint = tintColor, modifier = Modifier.size(21.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(notification.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    if (!notification.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(notification.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(5.dp))

                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    notification.timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                ).toString()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(relativeTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!notification.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.MarkEmailUnread, "Chưa đọc", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Chưa đọc", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissNotificationItem(
    notification: WarehouseNotification,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onDismiss()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val backgroundColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) {
                    androidx.compose.ui.graphics.Color.Transparent
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                },
                label = "NotificationDismissColor"
            )
            val alignment = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            Box(
                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(backgroundColor).padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                    Icon(Icons.Rounded.Delete, "Xóa thông báo", tint = MaterialTheme.colorScheme.onError)
                }
            }
        },
        content = { content() }
    )
}
