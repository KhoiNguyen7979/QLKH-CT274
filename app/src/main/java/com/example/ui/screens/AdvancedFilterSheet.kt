package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StockDanger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedProductFilterSheet(
    sheetState: SheetState,
    currentStockFilter: StockFilter,
    currentSortCriteria: SortCriteria,
    currentMinPrice: Double?,
    currentMaxPrice: Double?,
    currentMinQuantity: Int?,
    currentMaxQuantity: Int?,
    onDismiss: () -> Unit,
    onApply: (StockFilter, SortCriteria, Double?, Double?, Int?, Int?) -> Unit,
    onClear: () -> Unit
) {
    var tempStockFilter by remember(currentStockFilter) { mutableStateOf(currentStockFilter) }
    var tempSortCriteria by remember(currentSortCriteria) { mutableStateOf(currentSortCriteria) }
    var minPriceText by remember(currentMinPrice) { mutableStateOf(currentMinPrice?.let(::formatNumberInput) ?: "") }
    var maxPriceText by remember(currentMaxPrice) { mutableStateOf(currentMaxPrice?.let(::formatNumberInput) ?: "") }
    var minQuantityText by remember(currentMinQuantity) { mutableStateOf(currentMinQuantity?.toString() ?: "") }
    var maxQuantityText by remember(currentMaxQuantity) { mutableStateOf(currentMaxQuantity?.toString() ?: "") }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp)) {
            item {
                Text(text = "Bộ lọc & sắp xếp nâng cao", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SectionHeader(title = "Trạng thái tồn kho")
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(selected = tempStockFilter == StockFilter.ALL, onClick = { tempStockFilter = StockFilter.ALL }, label = { Text("Tất cả") })
                    }
                    item {
                        FilterChip(
                            selected = tempStockFilter == StockFilter.LOW_STOCK,
                            onClick = { tempStockFilter = StockFilter.LOW_STOCK },
                            label = { Text("Sắp hết ≤ $LOW_STOCK_THRESHOLD") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = StockDanger.copy(alpha = 0.12f), selectedLabelColor = StockDanger)
                        )
                    }
                    item {
                        FilterChip(selected = tempStockFilter == StockFilter.HIGH_STOCK, onClick = { tempStockFilter = StockFilter.HIGH_STOCK }, label = { Text("Tồn nhiều ≥ $HIGH_STOCK_THRESHOLD") })
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                WarehouseDivider()
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                SectionHeader(title = "Khoảng giá sản phẩm")
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = minPriceText, onValueChange = { minPriceText = sanitizeDecimalInput(it); validationMessage = null }, label = { Text("Giá từ") }, placeholder = { Text("0") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = maxPriceText, onValueChange = { maxPriceText = sanitizeDecimalInput(it); validationMessage = null }, label = { Text("Giá đến") }, placeholder = { Text("Không giới hạn") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                SectionHeader(title = "Khoảng số lượng tồn kho")
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = minQuantityText, onValueChange = { minQuantityText = it.filter { c -> c.isDigit() }; validationMessage = null }, label = { Text("Số lượng từ") }, placeholder = { Text("0") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = maxQuantityText, onValueChange = { maxQuantityText = it.filter { c -> c.isDigit() }; validationMessage = null }, label = { Text("Số lượng đến") }, placeholder = { Text("Không giới hạn") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Spacer(modifier = Modifier.height(18.dp))
                WarehouseDivider()
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                SectionHeader(title = "Sắp xếp theo tên")
                Spacer(modifier = Modifier.height(8.dp))
                ScrollableChipRow {
                    FilterChip(selected = tempSortCriteria == SortCriteria.NAME_ASC, onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.NAME_ASC) }, label = { Text("Tên A → Z") })
                    FilterChip(selected = tempSortCriteria == SortCriteria.NAME_DESC, onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.NAME_DESC) }, label = { Text("Tên Z → A") })
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SectionHeader(title = "Sắp xếp theo giá")
                Spacer(modifier = Modifier.height(8.dp))
                ScrollableChipRow {
                    FilterChip(selected = tempSortCriteria == SortCriteria.PRICE_ASC, onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.PRICE_ASC) }, label = { Text("Giá thấp → cao") })
                    FilterChip(selected = tempSortCriteria == SortCriteria.PRICE_DESC, onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.PRICE_DESC) }, label = { Text("Giá cao → thấp") })
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SectionHeader(title = "Sắp xếp theo số lượng")
                Spacer(modifier = Modifier.height(8.dp))
                ScrollableChipRow {
                    FilterChip(selected = tempSortCriteria == SortCriteria.QTY_ASC, onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.QTY_ASC) }, label = { Text("Ít → nhiều") })
                    FilterChip(selected = tempSortCriteria == SortCriteria.QTY_DESC, onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.QTY_DESC) }, label = { Text("Nhiều → ít") })
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            if (validationMessage != null) {
                item {
                    Text(text = validationMessage.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Xóa bộ lọc", color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            val minP = minPriceText.replace(",", ".").toDoubleOrNull()
                            val maxP = maxPriceText.replace(",", ".").toDoubleOrNull()
                            val minQ = minQuantityText.toIntOrNull()
                            val maxQ = maxQuantityText.toIntOrNull()
                            validationMessage = validateFilterRanges(minP, maxP, minQ, maxQ)
                            if (validationMessage == null) onApply(tempStockFilter, tempSortCriteria, minP, maxP, minQ, maxQ)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Áp dụng", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollableChipRow(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), content = content)
}
