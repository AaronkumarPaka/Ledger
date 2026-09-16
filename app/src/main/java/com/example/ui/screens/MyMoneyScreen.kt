package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EditExpenseDialog
import com.example.ui.components.LogExpenseDialog
import com.example.ui.components.ProjectionChart
import com.example.ui.components.RadarChart
import com.example.ui.components.SetIncomeDialog
import com.example.ui.theme.BrightBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryFun
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryRent
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryTravel
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MyMoneyUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun MyMoneyScreen(
    state: MyMoneyUiState,
    onSetIncome: (Double) -> Unit,
    onLogExpense: (String, Double, String, String) -> Unit,
    onUpdateExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSetIncomeDialog by remember { mutableStateOf(false) }
    var showLogExpenseDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("my_money_scroll"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. "This month" Overview Card
        item {
            ThisMonthCard(
                state = state,
                onSetIncomeClick = { showSetIncomeDialog = true }
            )
        }

        // 2. "Where it goes" Breakdown with Radar/Spider Chart
        item {
            WhereItGoesCard(state = state)
        }

        // 3. "If this pace holds" Projection Card
        item {
            ProjectionMilestonesCard(state = state)
        }

        // 4. "Expenses" Log List
        item {
            ExpensesLogCard(
                expenses = state.expenses,
                onLogExpenseClick = { showLogExpenseDialog = true },
                onEditExpense = { editingExpense = it },
                onDeleteExpense = { expenseToDelete = it }
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showSetIncomeDialog) {
        SetIncomeDialog(
            currentIncome = state.income,
            onDismiss = { showSetIncomeDialog = false },
            onConfirm = { newIncome ->
                onSetIncome(newIncome)
                showSetIncomeDialog = false
            }
        )
    }

    if (showLogExpenseDialog) {
        LogExpenseDialog(
            onDismiss = { showLogExpenseDialog = false },
            onConfirm = { title, amount, category, notes ->
                onLogExpense(title, amount, category, notes)
                showLogExpenseDialog = false
            }
        )
    }

    editingExpense?.let { expense ->
        EditExpenseDialog(
            expense = expense,
            onDismiss = { editingExpense = null },
            onConfirm = { updated ->
                onUpdateExpense(updated)
                editingExpense = null
            },
            onDelete = {
                onDeleteExpense(expense.id)
                editingExpense = null
            }
        )
    }

    expenseToDelete?.let { expense ->
        ConfirmDeleteDialog(
            title = "Delete Expense?",
            message = "Are you sure you want to delete '${expense.title}' (₹${"%,d".format(expense.amount.toInt())})?",
            onDismiss = { expenseToDelete = null },
            onConfirm = {
                onDeleteExpense(expense.id)
                expenseToDelete = null
            }
        )
    }
}

@Composable
private fun ThisMonthCard(
    state: MyMoneyUiState,
    onSetIncomeClick: () -> Unit
) {
    val animatedPercent = remember { Animatable(0f) }
    val fraction = (state.percentUsed / 100f).coerceIn(0f, 1f)

    LaunchedEffect(fraction) {
        animatedPercent.animateTo(fraction, animationSpec = tween(800))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("this_month_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Heading + Action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "This month",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.currentMonthYear,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = onSetIncomeClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrightBlue.copy(alpha = 0.12f),
                        contentColor = CyanAccent
                    ),
                    border = BorderStroke(1.dp, BrightBlue.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("set_income_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp), tint = CyanAccent)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Set monthly income", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Three key metric boxes: Income (₹18,000), Spent (₹6,320), Left (₹11,680)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox(
                    title = "Income",
                    amount = state.income,
                    color = BrightBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    title = "Spent",
                    amount = state.spent,
                    color = CoralRed,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    title = "Left",
                    amount = state.left,
                    color = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar: % of income used with "Day X of 30" indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.percentUsed}% of income used",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Day ${state.currentDay} of ${state.daysInMonth}",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

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
                        .fillMaxWidth(animatedPercent.value)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(BrightBlue, CyanAccent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic pace tracker badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = state.paceMessage,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${"%,d".format(amount.toInt())}",
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WhereItGoesCard(state: MyMoneyUiState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("where_it_goes_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Where it goes",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Category breakdown across all axes",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Radar / Spider Chart with topCategory highlight
            RadarChart(
                categories = state.radarData,
                topCategoryHighlight = state.topCategoryHighlight
            )
        }
    }
}

@Composable
private fun ProjectionMilestonesCard(state: MyMoneyUiState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("projection_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "If this pace holds",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Projected savings based on remaining monthly pace",
                color = TextMuted,
                fontSize = 11.sp
            )

            val monthlySavings = max(0.0, state.income - state.spent)
            ProjectionChart(
                monthlySavings = monthlySavings
            )
        }
    }
}

@Composable
private fun ExpensesLogCard(
    expenses: List<ExpenseEntity>,
    onLogExpenseClick: () -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expenses_log_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Expenses",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${expenses.size} recorded entries",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                OutlinedButton(
                    onClick = onLogExpenseClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrightBlue.copy(alpha = 0.15f),
                        contentColor = CyanAccent
                    ),
                    border = BorderStroke(1.dp, BrightBlue.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("log_expense_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanAccent)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Log expense", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses recorded for this month", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    expenses.forEach { expense ->
                        ExpenseItemRow(
                            expense = expense,
                            onEdit = { onEditExpense(expense) },
                            onDelete = { onDeleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseItemRow(
    expense: ExpenseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (expense.category.lowercase()) {
        "food" -> CategoryFood
        "travel" -> CategoryTravel
        "rent" -> CategoryRent
        "study" -> CategoryStudy
        "fun" -> CategoryFun
        else -> CategoryOther
    }

    val dateFormatted = remember(expense.timestamp) {
        SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()).format(Date(expense.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable { onEdit() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Category indicator dot
            Surface(
                modifier = Modifier.size(9.dp),
                shape = CircleShape,
                color = categoryColor
            ) {}

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = expense.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${expense.category} • $dateFormatted",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "₹${"%,d".format(expense.amount.toInt())}",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("edit_expense_${expense.id}")
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit expense",
                    tint = TextSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_expense_${expense.id}")
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete expense",
                    tint = CoralRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
