package com.example.proto7hive.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.proto7hive.ui.theme.BrandYellow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HexagonLoadingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    // Animasyon progress: 0f -> 1f
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    val backgroundColor = MaterialTheme.colorScheme.background
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Tüm ekranı tema rengine göre yap
        drawRect(
            color = backgroundColor,
            topLeft = Offset(0f, 0f),
            size = Size(width, height)
        )
        
        // Altıgen merkez noktası (ekranın ortasında)
        val hexCenterX = width / 2f
        val hexCenterY = height / 2f // Ekranın tam ortası
        val hexRadius = minOf(width, height) * 0.25f // Ekrana göre büyük altıgen
        
        // Altıgen path
        val hexPath = createHexagonPath(
            centerX = hexCenterX,
            centerY = hexCenterY,
            radius = hexRadius
        )
        
        // Altıgen outline (tema rengine göre, her zaman göster)
        drawPath(hexPath, color = outlineColor, style = Stroke(width = 4f))
        
        if (progress < 0.5f) {
            // İlk aşama: Altıgen doluyor (altından yukarıya)
            val hexFillProgress = progress * 2f // 0 -> 1.0 arası
            
            // Altıgen'i clip ederek doldur (aşağıdan yukarıya)
            clipPath(hexPath) {
                val hexBottom = hexCenterY + hexRadius
                val fillHeight = hexRadius * 2f * hexFillProgress
                val fillTop = hexBottom - fillHeight
                
                drawRect(
                    color = BrandYellow,
                    topLeft = Offset(hexCenterX - hexRadius, fillTop),
                    size = Size(hexRadius * 2f, fillHeight)
                )
            }
        } else {
            // İkinci aşama: Altıgen dolu, tüm ekran sarı oluyor (aşağıdan yukarıya)
            drawPath(hexPath, color = BrandYellow, style = Fill)
            
            // Tüm ekranı sarı ile doldur (aşağıdan yukarıya)
            val fillProgress = (progress - 0.5f) * 2f // 0.5 -> 1.0 arası
            val fillHeight = height * fillProgress
            drawRect(
                color = BrandYellow,
                topLeft = Offset(0f, height - fillHeight),
                size = Size(width, fillHeight)
            )
        }
    }
}

/**
 * Altıgen (hexagon) path oluşturur
 */
private fun createHexagonPath(centerX: Float, centerY: Float, radius: Float): Path {
    val path = Path()
    val angleStep = Math.PI / 3f // 60 derece
    
    for (i in 0..5) {
        val angle = i * angleStep - Math.PI / 2f // Başlangıç noktası üstte olsun
        val x = centerX + radius * cos(angle).toFloat()
        val y = centerY + radius * sin(angle).toFloat()
        
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    
    return path
}

/**
 * Basit kullanım için wrapper - Tam ekran siyah loading gösterir
 */
@Composable
fun CustomLoadingIndicator(
    modifier: Modifier = Modifier
) {
    HexagonLoadingIndicator(modifier = modifier.fillMaxSize())
}

