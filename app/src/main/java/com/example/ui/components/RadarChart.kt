package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryFun
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryRent
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryTravel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

data class RadarCategoryData(
    val category: String,
    val amount: Double,
    val color: Color
)

@Composable
fun RadarChart(
    categories: List<RadarCategoryData>,
    modifier: Modifier = Modifier,
    topCategoryHighlight: String = ""
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedFraction = remember { Animatable(0f) }
    var selectedCategory by remember { mutableStateOf<RadarCategoryData?>(null) }

    LaunchedEffect(categories) {
        animatedFraction.snapTo(0f)
        animatedFraction.animateTo(1f, animationSpec = tween(700))
    }

    val standardOrder = listOf("Food", "Travel", "Rent", "Study", "Fun", "Other")
    val defaultColors = mapOf(
        "Food" to CategoryFood,
        "Travel" to CategoryTravel,
        "Rent" to CategoryRent,
        "Study" to CategoryStudy,
        "Fun" to CategoryFun,
        "Other" to CategoryOther
    )

    // Ensure 6 categories exist
    val orderedData = remember(categories) {
        standardOrder.map { name ->
            val found = categories.find { it.category.equals(name, ignoreCase = true) }
            RadarCategoryData(
                category = name,
                amount = found?.amount ?: 0.0,
                color = defaultColors[name] ?: CategoryOther
            )
        }
    }

    val maxAmount = remember(orderedData) {
        max(1000.0, orderedData.maxOfOrNull { it.amount } ?: 1000.0)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Highlight badge at top
        if (topCategoryHighlight.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrightBlue.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrightBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(7.dp),
                            shape = CircleShape,
                            color = BrightBlue
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = topCategoryHighlight,
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .pointerInput(orderedData) {
                        detectTapGestures { offset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val radius = (minOf(size.width, size.height) / 2f) * 0.72f

                            val numPoints = orderedData.size
                            val angleStep = (2 * Math.PI) / numPoints

                            var nearest: RadarCategoryData? = null
                            var minDistance = Float.MAX_VALUE

                            orderedData.forEachIndexed { index, item ->
                                val angle = index * angleStep - (Math.PI / 2)
                                val x = (centerX + radius * cos(angle)).toFloat()
                                val y = (centerY + radius * sin(angle)).toFloat()
                                val dist = kotlin.math.hypot(offset.x - x, offset.y - y)
                                if (dist < 80f && dist < minDistance) {
                                    minDistance = dist
                                    nearest = item
                                }
                            }
                            selectedCategory = if (selectedCategory == nearest) null else nearest
                        }
                    }
            ) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val radius = (minOf(size.width, size.height) / 2f) * 0.68f

                val numPoints = orderedData.size
                val angleStep = (2 * Math.PI) / numPoints

                // 1. Concentric web polygons
                val webLevels = 4
                for (level in 1..webLevels) {
                    val levelRadius = radius * (level.toFloat() / webLevels)
                    val webPath = Path()
                    for (i in 0 until numPoints) {
                        val angle = i * angleStep - (Math.PI / 2)
                        val x = (centerX + levelRadius * cos(angle)).toFloat()
                        val y = (centerY + levelRadius * sin(angle)).toFloat()
                        if (i == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
                    }
                    webPath.close()
                    drawPath(
                        path = webPath,
                        color = Color(0xFF1E293B).copy(alpha = 0.7f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // 2. Axis lines from center to perimeter
                for (i in 0 until numPoints) {
                    val angle = i * angleStep - (Math.PI / 2)
                    val x = (centerX + radius * cos(angle)).toFloat()
                    val y = (centerY + radius * sin(angle)).toFloat()
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(centerX, centerY),
                        end = Offset(x, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 3. Data polygon
                val dataPath = Path()
                val dataPoints = mutableListOf<Offset>()

                orderedData.forEachIndexed { i, item ->
                    val angle = i * angleStep - (Math.PI / 2)
                    val normalizedValue = ((item.amount / maxAmount) * animatedFraction.value).coerceIn(0.08, 1.0)
                    val pointRadius = radius * normalizedValue.toFloat()
                    val x = (centerX + pointRadius * cos(angle)).toFloat()
                    val y = (centerY + pointRadius * sin(angle)).toFloat()
                    val point = Offset(x, y)
                    dataPoints.add(point)

                    if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()

                // Fill polygon
                drawPath(
                    path = dataPath,
                    color = BrightBlue.copy(alpha = 0.28f),
                    style = Fill
                )
                // Outline polygon
                drawPath(
                    path = dataPath,
                    color = CyanAccent,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 4. Draw data vertices and axis labels
                orderedData.forEachIndexed { i, item ->
                    val angle = i * angleStep - (Math.PI / 2)
                    val point = dataPoints[i]

                    // Vertex dot
                    drawCircle(
                        color = item.color,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.8.dp.toPx(),
                        center = point
                    )

                    // Outer Label calculation
                    val labelRadius = radius + 22.dp.toPx()
                    val labelX = (centerX + labelRadius * cos(angle)).toFloat()
                    val labelY = (centerY + labelRadius * sin(angle)).toFloat()

                    val textLayout = textMeasurer.measure(
                        text = item.category,
                        style = TextStyle(
                            color = if (selectedCategory?.category == item.category) CyanAccent else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selectedCategory?.category == item.category) FontWeight.Bold else FontWeight.Medium
                        )
                    )

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            labelX - (textLayout.size.width / 2f),
                            labelY - (textLayout.size.height / 2f)
                        )
                    )
                }
            }
        }

        // Details pill if clicked or default
        selectedCategory?.let { item ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = item.color
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.category}: ",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "₹${"%,d".format(item.amount.toInt())}",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        } ?: run {
            // Category badges legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                orderedData.take(3).forEach { item ->
                    LegendChip(item)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                orderedData.drop(3).forEach { item ->
                    LegendChip(item)
                }
            }
        }
    }
}

@Composable
private fun LegendChip(item: RadarCategoryData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Surface(
            modifier = Modifier.size(6.dp),
            shape = CircleShape,
            color = item.color
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${item.category} ₹${item.amount.toInt()}",
            color = TextMuted,
            fontSize = 10.sp
        )
    }
}
