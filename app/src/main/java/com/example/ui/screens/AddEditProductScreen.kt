package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Product
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    product: Product?,
    onBackClick: () -> Unit,
    onSubmit: (String, String, Int, Double, String, String, List<String>) -> Unit
) {
    val isEditMode = product != null
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var name by remember { mutableStateOf(product?.name ?: "") }
    var code by remember { mutableStateOf(product?.code ?: "") }
    var quantityStr by remember { mutableStateOf(product?.quantity?.toString() ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Chung") }
    var description by remember { mutableStateOf(product?.description ?: "") }

    var imageUrls by remember {
        mutableStateOf(
            product?.imageUrls?.let { it.take(4).plus(List(4 - it.size.coerceAtMost(4)) { "" }) }
                ?: List(4) { "" }
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(context.filesDir, "prod_${System.currentTimeMillis()}_$selectedIndex.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
            val newList = imageUrls.toMutableList()
            newList[selectedIndex] = file.absolutePath
            imageUrls = newList
        }
    }

    val categoryOptions = CategoryRegistry.categories.map { it.name }
    var categoryExpanded by remember { mutableStateOf(false) }

    var showErrorAlert by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val qty = quantityStr.toIntOrNull() ?: 0
    val price = priceStr.toDoubleOrNull() ?: 0.0
    val totalValue = qty * price

    fun generateRandomSKU() {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val prefix = (1..2).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        val suffix = (1..6).map { numbers[Random.nextInt(numbers.length)] }.joinToString("")
        code = "$prefix$suffix"
    }

    fun validateAndSubmit() {
        if (name.trim().isEmpty()) {
            errorMessage = "Vui lòng nhập tên sản phẩm!"
            showErrorAlert = true
        } else if (code.trim().isEmpty()) {
            errorMessage = "Vui lòng nhập mã sản phẩm!"
            showErrorAlert = true
        } else {
            onSubmit(name.trim(), code.trim(), quantityStr.toIntOrNull() ?: 0, priceStr.toDoubleOrNull() ?: 0.0, category, description.trim(), imageUrls.filter { it.isNotEmpty() })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditMode) "Sửa sản phẩm" else "Thêm sản phẩm", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { validateAndSubmit() }) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = "Lưu", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Image section
            SectionHeader(title = "HÌNH ẢNH SẢN PHẨM", modifier = Modifier.padding(bottom = 8.dp))

            WarehouseCard(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val pagerState = rememberPagerState(pageCount = { 4 })
                    val scope = rememberCoroutineScope()

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.small)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) { page ->
                        val url = imageUrls[page]
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (url.isNotEmpty() && !url.startsWith("preset_")) {
                                Image(painter = rememberAsyncImagePainter(File(url)), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(8.dp)) {
                                    Icon(imageVector = Icons.Rounded.AddAPhoto, contentDescription = "Thêm ảnh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "Hình ảnh ${page + 1}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Row(Modifier.height(20.dp).fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
                        repeat(4) { iteration ->
                            val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (index in 0 until 4) {
                            val url = imageUrls[index]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(MaterialTheme.shapes.small)
                                    .border(
                                        width = if (pagerState.currentPage == index) 2.dp else 1.dp,
                                        color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .clickable {
                                        selectedIndex = index
                                        launcher.launch("image/*")
                                        scope.launch { pagerState.animateScrollToPage(index) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (url.isNotEmpty() && !url.startsWith("preset_")) {
                                    Image(painter = rememberAsyncImagePainter(File(url)), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(imageVector = Icons.Rounded.Image, contentDescription = "Ô trống", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Form fields
            SectionHeader(title = "THÔNG TIN HÀNG HÓA", modifier = Modifier.padding(bottom = 8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên sản phẩm *") },
                placeholder = { Text("Nhập tên sản phẩm (ví dụ: Áo thun Polo)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Mã sản phẩm *") },
                placeholder = { Text("Mã SKU (ví dụ: TH363636)") },
                trailingIcon = {
                    IconButton(onClick = { generateRandomSKU() }) {
                        Icon(imageVector = Icons.Rounded.Autorenew, contentDescription = "Tạo mã ngẫu nhiên", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    label = { Text("Danh mục / Thư mục *") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                            Icon(
                                imageVector = if (categoryExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                                contentDescription = "Menu xổ xuống"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { category = option; categoryExpanded = false })
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Số lượng tồn kho *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Đơn giá (₫) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả sản phẩm") },
                placeholder = { Text("Nhập thông tin mô tả chi tiết, khu vực lưu trữ...") },
                modifier = Modifier.fillMaxWidth().height(110.dp).padding(bottom = 16.dp),
                shape = MaterialTheme.shapes.medium,
                maxLines = 4
            )

            // Total value calculator
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(text = "TỔNG TRỊ GIÁ SẢN PHẨM", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Công thức: Số lượng x Đơn giá", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(text = formatCurrency(totalValue), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showErrorAlert) {
            AlertDialog(
                onDismissRequest = { showErrorAlert = false },
                title = { Text("Lỗi nhập liệu") },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(onClick = { showErrorAlert = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                        Text("Đồng ý", color = MaterialTheme.colorScheme.onError)
                    }
                }
            )
        }
    }
}
