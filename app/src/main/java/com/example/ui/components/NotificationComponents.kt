package com.example.ui.components

import android.text.format.DateUtils
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.WarehouseNotification
import com.example.ui.theme.CategoryFolders
import com.example.ui.theme.StockDanger
import com.example.ui.theme.StockSafe
import com.example.ui.theme.StockWarning

@Composable
fun NotificationItemRow(
    notification: WarehouseNotification,
    onClick: () -> Unit
) {
    val (icon, tintColor) = when (notification.type) {
        "success" -> Icons.Rounded.CheckCircle to StockSafe
        "warning" -> Icons.Rounded.Warning to StockWarning
        "info" -> Icons.Rounded.Info to CategoryFolders
        else -> Icons.Rounded.Notifications to
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    .background(tintColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    notification.type,
                    tint = tintColor,
                    modifier = Modifier.size(21.dp)
                )
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
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(5.dp))

                val relativeTime = DateUtils
                    .getRelativeTimeSpanString(
                        notification.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                    .toString()

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        relativeTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!notification.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Rounded.MarkEmailUnread,
                            stringResource(R.string.notification_unread),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            stringResource(R.string.notification_unread),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
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
            if (value == SwipeToDismissBoxValue.EndToStart ||
                value == SwipeToDismissBoxValue.StartToEnd
            ) {
                onDismiss()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val backgroundColor by animateColorAsState(
                targetValue = if (dismissState.targetValue ==
                    SwipeToDismissBoxValue.Settled
                ) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                },
                label = "NotificationDismissColor"
            )
            val alignment = if (
                dismissState.targetValue ==
                    SwipeToDismissBoxValue.StartToEnd
            ) {
                Alignment.CenterStart
            } else {
                Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                if (dismissState.targetValue !=
                    SwipeToDismissBoxValue.Settled
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        stringResource(
                            R.string.content_desc_delete_notification
                        ),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        },
        content = { content() }
    )
}
