package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
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
    themeMode: Int,
    onSetThemeMode: (Int) -> Unit
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

    val themeLabels = listOf(
        stringResource(R.string.menu_theme_system),
        stringResource(R.string.menu_light),
        stringResource(R.string.menu_dark)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            WarehouseMenuButton(
                text = stringResource(R.string.menu_add_product),
                icon = Icons.Rounded.Add,
                onClick = { dismissAndRun(onAddProductClick) }
            )
            WarehouseMenuButton(
                text = stringResource(R.string.menu_search_product),
                icon = Icons.Rounded.Search,
                onClick = { dismissAndRun(onSearchItemsClick) }
            )
            WarehouseMenuButton(
                text = stringResource(R.string.menu_clear_notifications),
                icon = Icons.Rounded.DeleteForever,
                onClick = { onClearNotificationsClick() }
            )
            WarehouseMenuButton(
                text = stringResource(R.string.menu_add_random),
                icon = Icons.Rounded.Refresh,
                onClick = { onAddRandomItemsClick() }
            )

            WarehouseDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.menu_theme_system),
                    style = MaterialTheme.typography.titleMedium
                )
                themeLabels.forEachIndexed { index, label ->
                    FilterChip(
                        selected = themeMode == index,
                        onClick = { onSetThemeMode(index) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = null
                    )
                }
            }
        }
    }
}
