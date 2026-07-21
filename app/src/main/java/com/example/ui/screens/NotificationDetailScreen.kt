package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.WarehouseNotification
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notification: WarehouseNotification?,
    onBackClick: () -> Unit
) {
    if (notification == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.notification_detail_not_found),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val (icon, tintColor, typeLabel) = when (notification.type) {
        "success" -> Triple(
            Icons.Rounded.CheckCircle,
            StockSafe,
            stringResource(R.string.notification_type_success)
        )
        "warning" -> Triple(
            Icons.Rounded.Warning,
            StockWarning,
            stringResource(R.string.notification_type_warning)
        )
        "info" -> Triple(
            Icons.Rounded.Info,
            CategoryFolders,
            stringResource(R.string.notification_type_info)
        )
        else -> Triple(
            Icons.Rounded.Notifications,
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(R.string.notification_type_info)
        )
    }

    val dateFormat = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }
    val formattedDate = remember(notification.timestamp) {
        dateFormat.format(Date(notification.timestamp))
    }
    val relativeTime = remember(notification.timestamp) {
        DateUtils.getRelativeTimeSpanString(
            notification.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.notification_detail_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                stringResource(R.string.content_desc_back),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(tintColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = notification.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = tintColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = tintColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (!notification.isRead) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = stringResource(R.string.notification_unread),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 3.dp
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            WarehouseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.notification_detail_section_info)
                    )
                    Spacer(Modifier.height(8.dp))

                    DetailSpecRow(
                        icon = Icons.Rounded.Schedule,
                        label = stringResource(R.string.notification_detail_time),
                        value = relativeTime
                    )
                    WarehouseDivider()

                    DetailSpecRow(
                        icon = Icons.Rounded.CalendarToday,
                        label = stringResource(R.string.notification_detail_date),
                        value = formattedDate
                    )
                    WarehouseDivider()

                    DetailSpecRow(
                        icon = when (notification.type) {
                            "success" -> Icons.Rounded.CheckCircle
                            "warning" -> Icons.Rounded.Warning
                            else -> Icons.Rounded.Info
                        },
                        label = stringResource(R.string.notification_detail_type),
                        value = typeLabel,
                        valueColor = tintColor
                    )
                    WarehouseDivider()

                    DetailSpecRow(
                        icon = if (notification.isRead)
                            Icons.Rounded.MarkEmailRead
                        else
                            Icons.Rounded.MarkEmailUnread,
                        label = stringResource(R.string.notification_detail_status),
                        value = if (notification.isRead)
                            stringResource(R.string.notification_detail_status_read)
                        else
                            stringResource(R.string.notification_detail_status_unread),
                        valueColor = if (notification.isRead)
                            StockSafe
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            WarehouseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.notification_detail_section_content)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
