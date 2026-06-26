package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onClearAllClick: () -> Unit
) {
    // Separate into Today (less than 24 hours ago) and Earlier
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
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                        text = "You have no notification",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mọi hoạt động điều chỉnh tồn kho, cảnh báo hết hàng sẽ hiển thị tại đây.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                // NEW SECTION
                if (newNotifications.isNotEmpty()) {
                    item {
                        NotificationHeader("NEW")
                    }
                    items(newNotifications) { notification ->
                        NotificationItemRow(notification)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // EARLIER SECTION
                if (earlierNotifications.isNotEmpty()) {
                    item {
                        NotificationHeader("EARLIER")
                    }
                    items(earlierNotifications) { notification ->
                        NotificationItemRow(notification)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp)) // space for bottom navigation
                }
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
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.padding(vertical = 8.dp),
        letterSpacing = 1.sp
    )
}

@Composable
fun NotificationItemRow(notification: WarehouseNotification) {
    val (icon, tintColor) = when (notification.type) {
        "success" -> Pair(Icons.Rounded.CheckCircle, Color(0xFF4CAF50))
        "warning" -> Pair(Icons.Rounded.Warning, WarehouseRed)
        "info" -> Pair(Icons.Rounded.Info, CategoryFolders)
        else -> Pair(Icons.Rounded.Notifications, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tintColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = notification.type,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Human-readable relative timestamp (e.g., "Just now", "5 minutes ago")
                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    notification.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
                
                Text(
                    text = relativeTime,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}
