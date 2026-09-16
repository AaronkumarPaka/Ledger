package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun GaugeRing(
    settledPercent: Int,
    modifier: Modifier = Modifier,
    ringSize: Dp = 96.dp,
    strokeWidth: Dp = 9.dp
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(settledPercent) {
        animatedProgress.animateTo(
            targetValue = (settledPercent.coerceIn(0, 100) / 100f),
            animationSpec = tween(durationMillis = 900)
        )
    }

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background Track
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Settled Arc with Gradient
            val sweep = 360f * animatedProgress.value
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(CyanAccent, BrightBlue, Color(0xFF60A5FA), CyanAccent)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$settledPercent%",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "settled",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
