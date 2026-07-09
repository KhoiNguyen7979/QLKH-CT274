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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Product
import com.example.ui.theme.WarehouseRed
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

    // Form states
    var name by remember { mutableStateOf(product?.name ?: "") }
    var code by remember { mutableStateOf(product?.code ?: "") }
    var quantityStr by remember { mutableStateOf(product?.quantity?.toString() ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "Chung") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    
    // State for 4 image slots
    var imageUrls by remember { 
        mutableStateOf(
            product?.imageUrls?.let { it.take(4).plus(List(4 - it.size.coerceAtMost(4)) { "" }) } 
            ?: List(4) { "" }
        )
    }
    
    var selectedIndex by remember { mutableIntStateOf(0) }

    // Image Picker Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(context.filesDir, "prod_${System.currentTimeMillis()}_$selectedIndex.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            val newList = imageUrls.toMutableList()
            newList[selectedIndex] = file.absolutePath
            imageUrls = newList
        }
    }

    // Dropdown list category choices
    val categoryOptions = CategoryRegistry.categories.map { it.name }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Validation alerts
    var showErrorAlert by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Real-time Total Value calculator
    val qty = quantityStr.toIntOrNull() ?: 0
    val price = priceStr.toDoubleOrNull() ?: 0.0
    val totalValue = qty * price

    // Generate random unique code helper
    fun generateRandomSKU() {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val prefix = (1..2).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        val suffix = (1..6).map { numbers[Random.nextInt(numbers.length)] }.joinToString("")
        code = "$prefix$suffix"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Sửa sản phẩm" else "Thêm sản phẩm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarehouseRed)
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
            // IMAGE UPLOADER PLACEHOLDER (Matches Image 2 uploader mockup!)
            Text(
                text = "HÌNH ẢNH SẢN PHẨM",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp),
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val pagerState = rememberPagerState(pageCount = { 4 })
                    val scope = rememberCoroutineScope()
                    
                    // Main upload pager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                    ) { page ->
                        val url = imageUrls[page]
                        
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (url.isNotEmpty() && url.startsWith("preset_").not()) {
                                Image(
                                    painter = rememberAsyncImagePainter(File(url)),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AddAPhoto,
                                        contentDescription = "Add photos",
                                        tint = WarehouseRed,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Hình ảnh ${page + 1}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Page indicator for the editor
                    Row(
                        Modifier
                            .height(20.dp)
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(4) { iteration ->
                            val color = if (pagerState.currentPage == iteration) WarehouseRed else Color.LightGray
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Small slots row matching design uploader
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (index in 0 until 4) {
                            val url = imageUrls[index]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (pagerState.currentPage == index) 2.dp else 1.dp,
                                        color = if (pagerState.currentPage == index) WarehouseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                                    .clickable {
                                        selectedIndex = index
                                        launcher.launch("image/*")
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (url.isNotEmpty() && url.startsWith("preset_").not()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(File(url)),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Image,
                                        contentDescription = "Empty slot",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // FORM FIELDS
            Text(
                text = "THÔNG TIN HÀNG HÓA",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp),
                letterSpacing = 1.sp
            )

            // 1. Product Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên sản phẩm *") },
                placeholder = { Text("Nhập tên sản phẩm (ví dụ: Áo thun Polo)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            // 2. Product Code with Auto-Generator
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Mã sản phẩm *") },
                placeholder = { Text("Mã SKU (ví dụ: TH3636363636)") },
                trailingIcon = {
                    IconButton(onClick = { generateRandomSKU() }) {
                        Icon(
                            imageVector = Icons.Rounded.Autorenew,
                            contentDescription = "Tạo mã ngẫu nhiên",
                            tint = WarehouseRed
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            // 3. Category Dropdown Selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    label = { Text("Danh mục / Thư mục *") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                            Icon(
                                imageVector = if (categoryExpanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                                contentDescription = "Dropdown menu"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // 4. Quantity and Price Fields
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quantity field
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Số lượng tồn kho *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Price field
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Đơn giá ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            // 5. Product Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả sản phẩm") },
                placeholder = { Text("Nhập thông tin mô tả chi tiết, khu vực lưu trữ...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(10.dp),
                maxLines = 4
            )

            // LIVE TOTAL VALUE CALCULATOR BOX
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = WarehouseRed.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TỔNG TRỊ GIÁ SẢN PHẨM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarehouseRed
                        )
                        Text(
                            text = "Công thức: Số lượng x Đơn giá",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = formatCurrency(totalValue),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = WarehouseRed
                    )
                }
            }

            // SUBMIT BUTTON (Big red button matching Image 2 "Post" or "Add items" layout)
            Button(
                onClick = {
                    // Validations
                    if (name.trim().isEmpty()) {
                        errorMessage = "Vui lòng nhập tên sản phẩm!"
                        showErrorAlert = true
                    } else if (code.trim().isEmpty()) {
                        errorMessage = "Vui lòng nhập mã sản phẩm!"
                        showErrorAlert = true
                    } else {
                        val finalQty = quantityStr.toIntOrNull() ?: 0
                        val finalPrice = priceStr.toDoubleOrNull() ?: 0.0
                        
                        onSubmit(
                            name.trim(),
                            code.trim(),
                            finalQty,
                            finalPrice,
                            category,
                            description.trim(),
                            imageUrls.filter { it.isNotEmpty() }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarehouseRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEditMode) "CẬP NHẬT SẢN PHẨM" else "ADD ITEMS / ĐĂNG SẢN PHẨM",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Validation Error Alert Dialog
        if (showErrorAlert) {
            AlertDialog(
                onDismissRequest = { showErrorAlert = false },
                title = { Text("Lỗi nhập liệu") },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(
                        onClick = { showErrorAlert = false },
                        colors = ButtonDefaults.buttonColors(containerColor = WarehouseRed)
                    ) {
                        Text("Đồng ý", color = Color.White)
                    }
                }
            )
        }
    }
}
