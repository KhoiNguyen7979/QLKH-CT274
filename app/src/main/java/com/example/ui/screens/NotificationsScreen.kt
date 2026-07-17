package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WarehouseNotification
import com.example.ui.theme.CategoryFolders
import com.example.ui.theme.WarehouseRed

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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Thông báo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Badge(containerColor = WarehouseRed) {
                            Text(unreadCount.toString(), color = Color.White)
                        }
                    }
                }

                if (notifications.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAllClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = WarehouseRed)
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Xóa tất cả", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (unreadCount > 0) {
                TextButton(
                    onClick = onMarkAllAsReadClick,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Rounded.DoneAll, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Đánh dấu tất cả đã đọc", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (notifications.isNotEmpty()) {
                Text(
                    text = if (unreadCount > 0) {
                        "$unreadCount thông báo chưa đọc trong tổng số ${notifications.size}"
                    } else {
                        "Tất cả thông báo đã được đọc"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.NotificationsOff,
                            "Không có thông báo",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Bạn không có thông báo nào", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (newNotifications.isNotEmpty()) {
                    item { NotificationHeader("MỚI NHẤT", newNotifications.size) }
                    items(newNotifications, key = { it.id }) { notification ->
                        SwipeToDismissNotificationItem(
                            notification = notification,
                            onDismiss = { onNotificationDismissed(notification) }
                        ) {
                            NotificationItemRow(notification) {
                                if (!notification.isRead) onNotificationClick(notification)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }

                if (earlierNotifications.isNotEmpty()) {
                    item { NotificationHeader("TRƯỚC ĐÓ", earlierNotifications.size) }
                    items(earlierNotifications, key = { it.id }) { notification ->
                        SwipeToDismissNotificationItem(
                            notification = notification,
                            onDismiss = { onNotificationDismissed(notification) }
                        ) {
                            NotificationItemRow(notification) {
                                if (!notification.isRead) onNotificationClick(notification)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun NotificationHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )
        Spacer(Modifier.width(6.dp))
        Text("($count)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NotificationItemRow(
    notification: WarehouseNotification,
    onClick: () -> Unit
) {
    val (icon, tintColor) = when (notification.type) {
        "success" -> Icons.Rounded.CheckCircle to Color(0xFF4CAF50)
        "warning" -> Icons.Rounded.Warning to WarehouseRed
        "info" -> Icons.Rounded.Info to CategoryFolders
        else -> Icons.Rounded.Notifications to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                WarehouseRed.copy(alpha = 0.08f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(tintColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, notification.type, tint = tintColor, modifier = Modifier.size(21.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        notification.title,
                        fontSize = 14.sp,
                        fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.ExtraBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(WarehouseRed)
                        )
                    }
                }

                Spacer(Modifier.height(3.dp))
                Text(
                    notification.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (notification.isRead) 0.65f else 0.85f
                    )
                )
                Spacer(Modifier.height(5.dp))

                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    notification.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        relativeTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    if (!notification.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Rounded.MarkEmailUnread,
                            "Chưa đọc",
                            tint = WarehouseRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text("Chưa đọc", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarehouseRed)
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
            if (
                value == SwipeToDismissBoxValue.EndToStart ||
                value == SwipeToDismissBoxValue.StartToEnd
            ) {
                onDismiss()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val backgroundColor by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) {
                    Color.Transparent
                } else {
                    WarehouseRed.copy(alpha = 0.9f)
                },
                label = "NotificationDismissColor"
            )
            val alignment = if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                Alignment.CenterStart
            } else {
                Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                    Icon(Icons.Rounded.Delete, "Xóa thông báo", tint = Color.White)
                }
            }
        },
        content = { content() }
    )
}
