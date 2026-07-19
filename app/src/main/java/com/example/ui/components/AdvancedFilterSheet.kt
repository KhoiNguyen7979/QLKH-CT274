package com.example.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.screens.HIGH_STOCK_THRESHOLD
import com.example.ui.screens.LOW_STOCK_THRESHOLD
import com.example.ui.screens.StockFilter
import com.example.ui.screens.SortCriteria
import com.example.ui.screens.formatNumberInput
import com.example.ui.screens.toggleSortCriteria
import com.example.ui.screens.validateFilterRanges
import com.example.ui.screens.sanitizeDecimalInput
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
    var validationMessageRes by remember { mutableStateOf<Int?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp)
        ) {
            item {
                Text(text = stringResource(R.string.filter_sheet_title), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                SectionHeader(title = stringResource(R.string.filter_stock_status))
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = tempStockFilter == StockFilter.ALL,
                            onClick = { tempStockFilter = StockFilter.ALL },
                            label = { Text(stringResource(R.string.filter_all)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = tempStockFilter == StockFilter.LOW_STOCK,
                            onClick = { tempStockFilter = StockFilter.LOW_STOCK },
                            label = { Text(stringResource(R.string.filter_low_stock, LOW_STOCK_THRESHOLD)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StockDanger.copy(alpha = 0.12f),
                                selectedLabelColor = StockDanger
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = tempStockFilter == StockFilter.HIGH_STOCK,
                            onClick = { tempStockFilter = StockFilter.HIGH_STOCK },
                            label = { Text(stringResource(R.string.filter_high_stock, HIGH_STOCK_THRESHOLD)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                WarehouseDivider()
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                SectionHeader(title = stringResource(R.string.filter_price_range))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minPriceText,
                        onValueChange = {
                            minPriceText = sanitizeDecimalInput(it)
                            validationMessageRes = null
                        },
                        label = { Text(stringResource(R.string.filter_price_from)) },
                        placeholder = { Text(stringResource(R.string.filter_placeholder_zero)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = maxPriceText,
                        onValueChange = {
                            maxPriceText = sanitizeDecimalInput(it)
                            validationMessageRes = null
                        },
                        label = { Text(stringResource(R.string.filter_price_to)) },
                        placeholder = { Text(stringResource(R.string.filter_placeholder_unlimited)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                SectionHeader(title = stringResource(R.string.filter_qty_range))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minQuantityText,
                        onValueChange = {
                            minQuantityText = it.filter { c -> c.isDigit() }
                            validationMessageRes = null
                        },
                        label = { Text(stringResource(R.string.filter_qty_from)) },
                        placeholder = { Text(stringResource(R.string.filter_placeholder_zero)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = maxQuantityText,
                        onValueChange = {
                            maxQuantityText = it.filter { c -> c.isDigit() }
                            validationMessageRes = null
                        },
                        label = { Text(stringResource(R.string.filter_qty_to)) },
                        placeholder = { Text(stringResource(R.string.filter_placeholder_unlimited)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                WarehouseDivider()
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                SectionHeader(title = stringResource(R.string.filter_sort_by_name))
                Spacer(modifier = Modifier.height(8.dp))
                ScrollableChipRow {
                    FilterChip(
                        selected = tempSortCriteria == SortCriteria.NAME_ASC,
                        onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.NAME_ASC) },
                        label = { Text(stringResource(R.string.filter_sort_name_asc)) }
                    )
                    FilterChip(
                        selected = tempSortCriteria == SortCriteria.NAME_DESC,
                        onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.NAME_DESC) },
                        label = { Text(stringResource(R.string.filter_sort_name_desc)) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SectionHeader(title = stringResource(R.string.filter_sort_by_price))
                Spacer(modifier = Modifier.height(8.dp))
                ScrollableChipRow {
                    FilterChip(
                        selected = tempSortCriteria == SortCriteria.PRICE_ASC,
                        onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.PRICE_ASC) },
                        label = { Text(stringResource(R.string.filter_sort_price_asc)) }
                    )
                    FilterChip(
                        selected = tempSortCriteria == SortCriteria.PRICE_DESC,
                        onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.PRICE_DESC) },
                        label = { Text(stringResource(R.string.filter_sort_price_desc)) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SectionHeader(title = stringResource(R.string.filter_sort_by_qty))
                Spacer(modifier = Modifier.height(8.dp))
                ScrollableChipRow {
                    FilterChip(
                        selected = tempSortCriteria == SortCriteria.QTY_ASC,
                        onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.QTY_ASC) },
                        label = { Text(stringResource(R.string.filter_sort_qty_asc)) }
                    )
                    FilterChip(
                        selected = tempSortCriteria == SortCriteria.QTY_DESC,
                        onClick = { tempSortCriteria = toggleSortCriteria(tempSortCriteria, SortCriteria.QTY_DESC) },
                        label = { Text(stringResource(R.string.filter_sort_qty_desc)) }
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            if (validationMessageRes != null) {
                item {
                    Text(
                        text = validationMessageRes?.let { stringResource(it) }.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.btn_clear_filter),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = {
                            val minP = minPriceText.replace(",", ".").toDoubleOrNull()
                            val maxP = maxPriceText.replace(",", ".").toDoubleOrNull()
                            val minQ = minQuantityText.toIntOrNull()
                            val maxQ = maxQuantityText.toIntOrNull()
                            validationMessageRes = validateFilterRanges(minP, maxP, minQ, maxQ)
                            if (validationMessageRes == null) {
                                onApply(
                                    tempStockFilter,
                                    tempSortCriteria,
                                    minP,
                                    maxP,
                                    minQ,
                                    maxQ
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.btn_apply_filter),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollableChipRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}
