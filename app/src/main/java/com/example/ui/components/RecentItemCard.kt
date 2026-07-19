package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.Product
import com.example.ui.theme.StockDanger
import com.example.ui.utils.CategoryRegistry
import com.example.ui.utils.isLowStock
import java.io.File

@Composable
fun RecentItemCard(product: Product, onClick: () -> Unit) {
    val meta = CategoryRegistry.getMeta(product.category)

    WarehouseCard(
        modifier = Modifier.width(150.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(meta.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrls.isNotEmpty() &&
                    !product.imageUrls.first().startsWith("preset_")
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            File(product.imageUrls.first())
                        ),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = meta.icon,
                            contentDescription = product.name,
                            tint = meta.color,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.product_code_short_label,
                                product.code.take(6)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = meta.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(
                    R.string.product_qty_unit,
                    product.quantity
                ),
                style = MaterialTheme.typography.labelLarge,
                color = if (product.isLowStock()) {
                    StockDanger
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )
        }
    }
}
