package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max

enum class ProjectionHorizon(val label: String, val months: Int, val shortLabel: String) {
    THREE_MONTHS("3 Months", 3, "3 Mo"),
    ONE_YEAR("1 Year", 12, "1 Yr"),
    TWO_YEARS("2 Years", 24, "2 Yrs"),
    FIVE_YEARS("5 Years", 60, "5 Yrs")
}

@Composable
fun ProjectionChart(
    monthlySavings: Double,
    modifier: Modifier = Modifier
) {
    var selectedHorizon by remember { mutableStateOf(ProjectionHorizon.ONE_YEAR) }
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    val safeSavings = max(100.0, monthlySavings)

    LaunchedEffect(selectedHorizon, safeSavings) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(600))
    }

    val projectedTotal = safeSavings * selectedHorizon.months

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Horizon selector chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProjectionHorizon.values().forEach { horizon ->
                val isSelected = horizon == selectedHorizon
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BrightBlue else CardSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BrightBlue else CardBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedHorizon = horizon }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = horizon.shortLabel,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Selected Projection Highlight
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Projected in ${selectedHorizon.label}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "₹${"%,d".format(projectedTotal.toLong())}",
                    color = EmeraldGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(6.dp),
                        shape = CircleShape,
                        color = BrightBlue
                    ) {}
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "₹${"%,d".format(safeSavings.toLong())}/mo pace",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Canvas Line Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(top = 8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val padLeft = 16.dp.toPx()
                val padRight = 24.dp.toPx()
                val padBottom = 26.dp.toPx()
                val padTop = 14.dp.toPx()

                val chartW = size.width - padLeft - padRight
                val chartH = size.height - padTop - padBottom

                val milestones = ProjectionHorizon.values()
                val maxMonths = 60f
                val maxVal = (safeSavings * 60).toFloat()

                // Draw Horizontal subtle grid lines (3 levels)
                for (i in 0..3) {
                    val y = padTop + chartH * (i / 3f)
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(padLeft, y),
                        end = Offset(size.width - padRight, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Points along the projection curve
                val points = mutableListOf<Pair<ProjectionHorizon, Offset>>()
                // Origin
                val origin = Offset(padLeft, padTop + chartH)

                milestones.forEach { h ->
                    val x = padLeft + (h.months / maxMonths) * chartW
                    val value = (safeSavings * h.months).toFloat()
                    val y = padTop + chartH - (value / maxVal) * chartH
                    points.add(Pair(h, Offset(x, y)))
                }

                // Smooth Path connecting origin through milestones
                val linePath = Path()
                val fillPath = Path()

                linePath.moveTo(origin.x, origin.y)
                fillPath.moveTo(origin.x, origin.y)

                var prevX = origin.x
                var prevY = origin.y

                val animatedHorizonIdx = milestones.indexOf(selectedHorizon)

                points.forEachIndexed { idx, pair ->
                    val pt = pair.second
                    // Cubic interpolation for curved trajectory
                    val cX1 = prevX + (pt.x - prevX) / 2f
                    val cY1 = prevY
                    val cX2 = prevX + (pt.x - prevX) / 2f
                    val cY2 = pt.y

                    linePath.cubicTo(cX1, cY1, cX2, cY2, pt.x, pt.y)
                    fillPath.cubicTo(cX1, cY1, cX2, cY2, pt.x, pt.y)

                    prevX = pt.x
                    prevY = pt.y
                }

                val lastPt = points.last().second
                fillPath.lineTo(lastPt.x, origin.y)
                fillPath.close()

                // Draw gradient under curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(BrightBlue.copy(alpha = 0.28f * animatedProgress.value), Color.Transparent),
                        startY = padTop,
                        endY = origin.y
                    ),
                    style = Fill
                )

                // Draw main curve stroke
                drawPath(
                    path = linePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(BrightBlue, CyanAccent, EmeraldGreen)
                    ),
                    style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw milestone points & X-axis labels
                points.forEach { (h, pt) ->
                    val isSelected = h == selectedHorizon

                    // Milestone dot
                    drawCircle(
                        color = if (isSelected) EmeraldGreen else BrightBlue,
                        radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx(),
                        center = pt
                    )

                    // Draw milestone label beneath X-axis
                    val labelResult = textMeasurer.measure(
                        text = h.shortLabel,
                        style = TextStyle(
                            color = if (isSelected) TextPrimary else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(
                            pt.x - (labelResult.size.width / 2f),
                            padTop + chartH + 6.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}
