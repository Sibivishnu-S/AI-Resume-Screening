package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AtsGreen
import com.example.ui.theme.AtsRed
import com.example.ui.theme.AtsAmber
import com.example.ui.theme.SecondaryCyan

@Composable
fun CircularAtsGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp
) {
    var animationTarget by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(score) {
        animationTarget = (score.coerceIn(0, 100) / 100f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "AtsScoreGaugeAnimation"
    )

    val gaugeColor = when {
        score >= 85 -> AtsGreen
        score >= 70 -> SecondaryCyan
        score >= 50 -> AtsAmber
        else -> AtsRed
    }

    val tierText = when {
        score >= 85 -> "EXCELLENT FIT"
        score >= 70 -> "GOOD MATCH"
        score >= 50 -> "MODERATE FIT"
        else -> "NEEDS WORK"
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()

            // Background arc (240 degrees sweep)
            drawArc(
                color = trackColor,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated Progress Arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        gaugeColor.copy(alpha = 0.6f),
                        gaugeColor
                    )
                ),
                startAngle = 150f,
                sweepAngle = 240f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).toInt()}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "OUT OF 100",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = tierText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
        }
    }
}
