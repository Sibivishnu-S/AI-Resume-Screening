package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AtsGreen
import com.example.ui.theme.AtsRed
import com.example.ui.theme.AtsAmber
import com.example.ui.theme.SecondaryCyan

data class BarItemData(
    val label: String,
    val score: Int,
    val weightText: String
)

@Composable
fun ScoreBreakdownBarChart(
    items: List<BarItemData>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEach { item ->
            BarRow(item = item)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BarRow(item: BarItemData) {
    var animationTarget by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(item.score) {
        animationTarget = item.score.coerceIn(0, 100) / 100f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 1000),
        label = "BarChartAnimation"
    )

    val barColor = when {
        item.score >= 80 -> AtsGreen
        item.score >= 65 -> SecondaryCyan
        item.score >= 45 -> AtsAmber
        else -> AtsRed
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = item.weightText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${item.score}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val barWidth = size.width
            val barHeight = size.height

            // Background track
            drawRoundRect(
                color = trackColor,
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
            )

            // Progress bar
            drawRoundRect(
                color = barColor,
                size = Size(barWidth * animatedProgress, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
            )
        }
    }
}
