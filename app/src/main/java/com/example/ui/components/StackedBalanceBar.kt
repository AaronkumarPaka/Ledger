package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LightCoral
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.max

data class OpenBalanceItem(
    val friendId: Long,
    val friendName: String,
    val amount: Double, // net balance: > 0 means friend owes user, < 0 means user owes friend
    val contact: String = ""
)

@Composable
fun StackedBalanceBar(
    items: List<OpenBalanceItem>,
    modifier: Modifier = Modifier,
    onItemClick: (OpenBalanceItem) -> Unit = {}
) {
    // Filter out settled (amount == 0) and sort by absolute balance descending
    val nonZero = remember(items) {
        items.filter { abs(it.amount) > 0.01 }.sortedByDescending { abs(it.amount) }
    }

    val maxAbsAmount = remember(nonZero) {
        max(1.0, nonZero.maxOfOrNull { abs(it.amount) } ?: 1.0)
    }

    if (nonZero.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "All tabs are fully settled! 🎉",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        nonZero.forEach { item ->
            OpenBalanceRow(
                item = item,
                maxAmount = maxAbsAmount,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@Composable
private fun OpenBalanceRow(
    item: OpenBalanceItem,
    maxAmount: Double,
    onClick: () -> Unit
) {
    val isOwedToUser = item.amount > 0
    val absAmount = abs(item.amount)
    val fraction = (absAmount / maxAmount).toFloat().coerceIn(0.05f, 1f)

    val animatedWidth = remember { Animatable(0f) }

    LaunchedEffect(fraction) {
        animatedWidth.animateTo(fraction, animationSpec = tween(700))
    }

    val barBrush = if (isOwedToUser) {
        Brush.horizontalGradient(listOf(BrightBlue, CyanAccent))
    } else {
        Brush.horizontalGradient(listOf(CoralRed, LightCoral))
    }

    val statusColor = if (isOwedToUser) BrightBlue else CoralRed
    val statusText = if (isOwedToUser) "Owes you" else "You owe"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        // Label header: Friend Name + Status Tag + Amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Initial circle
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = item.friendName.take(1).uppercase(),
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.friendName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "₹${"%,d".format(absAmount.toInt())}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress visual bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedWidth.value)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barBrush)
            )
        }
    }
}
