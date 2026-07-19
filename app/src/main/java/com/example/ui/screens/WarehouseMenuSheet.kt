package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseMenuSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAddProductClick: () -> Unit,
    onSearchItemsClick: () -> Unit,
    onClearNotificationsClick: () -> Unit,
    onAddRandomItemsClick: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    fun dismissAndRun(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
                action()
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface, dragHandle = { BottomSheetDefaults.DragHandle() }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            WarehouseMenuButton(text = "Thêm sản phẩm", icon = Icons.Rounded.Add, onClick = { dismissAndRun(onAddProductClick) })
            WarehouseMenuButton(text = "Tìm kiếm sản phẩm", icon = Icons.Rounded.Search, onClick = { dismissAndRun(onSearchItemsClick) })
            WarehouseMenuButton(text = "Xóa thông báo", icon = Icons.Rounded.DeleteForever, onClick = { dismissAndRun(onClearNotificationsClick) })
            WarehouseMenuButton(text = "Thêm mẫu ngẫu nhiên", icon = Icons.Rounded.Refresh, onClick = { dismissAndRun(onAddRandomItemsClick) })
            WarehouseMenuButton(
                text = if (isDarkMode) "Tắt chế độ tối" else "Bật chế độ tối",
                icon = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                onClick = { dismissAndRun(onToggleDarkMode) }
            )
        }
    }
}
