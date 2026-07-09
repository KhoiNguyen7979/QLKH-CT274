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
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<WarehouseNotification>,
    onClearAllClick: () -> Unit,
    // Ép kiểu cụ thể dạng Lambda để Kotlin không bao giờ bị infer sai type
    onNotificationDismissed: (WarehouseNotification) -> Unit = {},
    onNotificationClick: (WarehouseNotification) -> Unit = {}
) {
    val currentTime = System.currentTimeMillis()
    val oneDayMillis = 24 * 60 * 60 * 1000L

    val (newNotifications, earlierNotifications) = notifications.partition {
        currentTime - it.timestamp < oneDayMillis
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Thông báo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (notifications.isNotEmpty()) {
                TextButton(
                    onClick = onClearAllClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = WarehouseRed)
                ) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa tất cả", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsOff,
                            contentDescription = "No notifications icon",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bạn không có thông báo nào",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                if (newNotifications.isNotEmpty()) {
                    item { NotificationHeader("MỚI NHẤT") }
                    items(newNotifications, key = { it.id }) { notification: WarehouseNotification ->
                        SwipeToDismissItem(
                            notification = notification,
                            onDismiss = { onNotificationDismissed(notification) },
                            content = {
                                NotificationItemRow(
                                    notification = notification,
                                    onClick = { onNotificationClick(notification) }
                                )
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                if (earlierNotifications.isNotEmpty()) {
                    item { NotificationHeader("TRƯỚC ĐÓ") }
                    items(earlierNotifications, key = { it.id }) { notification: WarehouseNotification ->
                        SwipeToDismissItem(
                            notification = notification,
                            onDismiss = { onNotificationDismissed(notification) },
                            content = {
                                NotificationItemRow(
                                    notification = notification,
                                    onClick = { onNotificationClick(notification) }
                                )
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun NotificationHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        modifier = Modifier.padding(vertical = 8.dp),
        letterSpacing = 1.sp
    )
}

@Composable
fun NotificationItemRow(
    notification: WarehouseNotification,
    onClick: () -> Unit
) {
    val (icon, tintColor) = when (notification.type) {
        "success" -> Pair(Icons.Rounded.CheckCircle, Color(0xFF4CAF50))
        "warning" -> Pair(Icons.Rounded.Warning, WarehouseRed)
        "info" -> Pair(Icons.Rounded.Info, CategoryFolders)
        else -> Pair(Icons.Rounded.Notifications, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tintColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = notification.type, tint = tintColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    notification.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()

                Text(text = relativeTime, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissItem(
    notification: WarehouseNotification,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                onDismiss()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    else -> WarehouseRed.copy(alpha = 0.9f)
                }, label = "ColorAnimation"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                    Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        },
        content = { content() }
    )
}