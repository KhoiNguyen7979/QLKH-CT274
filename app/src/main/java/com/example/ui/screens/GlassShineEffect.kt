package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Hiệu ứng kính sáng dùng chung cho các thẻ sản phẩm.
 * Gồm một lớp ánh sáng rộng, một lõi sáng rõ và viền kính nhẹ.
 */
fun Modifier.strongGlassShine(): Modifier = composed {
    val transition = rememberInfiniteTransition(
        label = "StrongGlassTransition"
    )

    val translateX by transition.animateFloat(
        initialValue = -700f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1650,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "StrongGlassTranslate"
    )

    drawWithContent {
        drawContent()

        // Lớp ánh sáng rộng tạo cảm giác mặt kính.
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.04f),
                    Color.White.copy(alpha = 0.18f),
                    Color.White.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                start = Offset(translateX - 180f, 0f),
                end = Offset(translateX + 380f, size.height)
            )
        )

        // Lõi sáng hẹp giúp vệt kính nhìn rõ hơn.
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.62f),
                    Color.White.copy(alpha = 0.20f),
                    Color.Transparent
                ),
                start = Offset(translateX, 0f),
                end = Offset(translateX + 180f, size.height)
            )
        )

        // Viền sáng nhẹ làm thẻ nổi rõ như kính.
        drawRoundRect(
            color = Color.White.copy(alpha = 0.16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
            style = Stroke(width = 1.2f)
        )
    }
}
