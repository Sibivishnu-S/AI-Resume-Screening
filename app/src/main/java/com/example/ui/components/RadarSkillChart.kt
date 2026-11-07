package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.SecondaryCyan
import kotlin.math.cos
import kotlin.math.sin

data class RadarDimension(
    val name: String,
    val valuePercent: Float // 0f to 1f
)

@Composable
fun RadarSkillChart(
    dimensions: List<RadarDimension>,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    if (dimensions.isEmpty()) return

    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val polygonFillColor = SecondaryCyan.copy(alpha = 0.35f)
    val polygonStrokeColor = PrimaryLight

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = (this.size.width / 2) * 0.75f
            val sides = dimensions.size
            val angleStep = (2 * Math.PI / sides).toFloat()

            // Draw concentric web rings (25%, 50%, 75%, 100%)
            val ringSteps = listOf(0.25f, 0.50f, 0.75f, 1.0f)
            ringSteps.forEach { stepRatio ->
                val ringPath = Path()
                for (i in 0 until sides) {
                    val angle = (i * angleStep) - (Math.PI / 2).toFloat()
                    val x = center.x + (radius * stepRatio * cos(angle))
                    val y = center.y + (radius * stepRatio * sin(angle))
                    if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
                }
                ringPath.close()
                drawPath(ringPath, gridColor, style = Stroke(width = 2f))
            }

            // Draw axis spoke lines
            for (i in 0 until sides) {
                val angle = (i * angleStep) - (Math.PI / 2).toFloat()
                val x = center.x + (radius * cos(angle))
                val y = center.y + (radius * sin(angle))
                drawLine(gridColor, center, Offset(x, y), strokeWidth = 2f)
            }

            // Draw Data Polygon
            val dataPath = Path()
            dimensions.forEachIndexed { i, dim ->
                val angle = (i * angleStep) - (Math.PI / 2).toFloat()
                val clampedVal = dim.valuePercent.coerceIn(0.1f, 1.0f)
                val x = center.x + (radius * clampedVal * cos(angle))
                val y = center.y + (radius * clampedVal * sin(angle))
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            // Fill & Stroke
            drawPath(dataPath, polygonFillColor)
            drawPath(dataPath, polygonStrokeColor, style = Stroke(width = 5f))

            // Draw vertex dots
            dimensions.forEachIndexed { i, dim ->
                val angle = (i * angleStep) - (Math.PI / 2).toFloat()
                val clampedVal = dim.valuePercent.coerceIn(0.1f, 1.0f)
                val x = center.x + (radius * clampedVal * cos(angle))
                val y = center.y + (radius * clampedVal * sin(angle))
                drawCircle(polygonStrokeColor, radius = 8f, center = Offset(x, y))
            }
        }
    }
}
