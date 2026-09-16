package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.ExpenseEntity
import com.example.data.FriendEntity
import com.example.data.SplitEntity
import com.example.ui.components.OpenBalanceItem
import com.example.ui.components.RadarCategoryData
import com.example.ui.theme.CategoryFood
import com.example.ui.theme.CategoryFun
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryRent
import com.example.ui.theme.CategoryStudy
import com.example.ui.theme.CategoryTravel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max

data class FriendWithBalance(
    val friend: FriendEntity,
    val netBalance: Double // > 0 friend owes user, < 0 user owes friend
)

data class SharedTabsUiState(
    val comingToYou: Double = 855.0,
    val youStillOwe: Double = 1250.0,
    val netBalance: Double = -395.0,
    val settledPercent: Int = 17,
    val friendsWithBalances: List<FriendWithBalance> = emptyList(),
    val openBalances: List<OpenBalanceItem> = emptyList(),
    val recentSplits: List<SplitEntity> = emptyList(),
    val allFriends: List<FriendEntity> = emptyList()
)

data class MyMoneyUiState(
    val currentMonthYear: String = "September 2026",
    val income: Double = 18000.0,
    val spent: Double = 6320.0,
    val left: Double = 11680.0,
    val percentUsed: Int = 35,
    val currentDay: Int = 16,
    val daysInMonth: Int = 30,
    val daysRemaining: Int = 14,
    val dailyBudget: Double = 834.0,
    val paceMessage: String = "On pace. ₹11,680 left with 14 days to go — that is about ₹834 a day.",
    val radarData: List<RadarCategoryData> = emptyList(),
    val topCategoryHighlight: String = "Rent leads",
    val expenses: List<ExpenseEntity> = emptyList()
)

data class CoachMessage(
    val id: Long = System.currentTimeMillis(),
    val query: String,
    val response: String,
    val isOnline: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AppRepository(
            database.expenseDao(),
            database.friendDao(),
            database.splitDao(),
            database.userSettingsDao(),
            database
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedIfEmpty()
        }
    }

    // Navigation and app state
    private val _selectedTab = MutableStateFlow(0) // 0: Shared tabs, 1: My money
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Savings Coach state
    private val _isOnlineCoachMode = MutableStateFlow(false)
    val isOnlineCoachMode: StateFlow<Boolean> = _isOnlineCoachMode.asStateFlow()

    private val _coachMessages = MutableStateFlow<List<CoachMessage>>(
        listOf(
            CoachMessage(
                query = "Hello Coach!",
                response = "Hi! I'm your LedgerLink Savings Coach. I can analyze your split tabs, pace your monthly spending, and suggest actionable savings strategies."
            )
        )
    )
    val coachMessages: StateFlow<List<CoachMessage>> = _coachMessages.asStateFlow()

    private val _isCoachThinking = MutableStateFlow(false)
    val isCoachThinking: StateFlow<Boolean> = _isCoachThinking.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleCoachOnlineMode() {
        _isOnlineCoachMode.value = !_isOnlineCoachMode.value
    }

    // Shared Tabs UI State Flow
    val sharedTabsState: StateFlow<SharedTabsUiState> = combine(
        repository.allFriends,
        repository.allSplits,
        repository.recentSplits
    ) { friends, allSplits, recentSplits ->
        val unsettled = allSplits.filter { !it.isSettled }
        val settled = allSplits.filter { it.isSettled }

        val comingToYou = unsettled.filter { it.isOwedToUser }.sumOf { it.amount }
        val youStillOwe = unsettled.filter { !it.isOwedToUser }.sumOf { it.amount }
        val netBalance = comingToYou - youStillOwe

        val totalCount = allSplits.size
        val settledPercent = if (totalCount > 0) {
            ((settled.size.toDouble() / totalCount) * 100).toInt()
        } else {
            0
        }

        // Calculate per-friend balances
        val friendsWithBalances = friends.map { friend ->
            val friendSplits = unsettled.filter { it.friendId == friend.id }
            val owedToUser = friendSplits.filter { it.isOwedToUser }.sumOf { it.amount }
            val userOwes = friendSplits.filter { !it.isOwedToUser }.sumOf { it.amount }
            val net = owedToUser - userOwes
            FriendWithBalance(friend, net)
        }

        val openBalances = friendsWithBalances
            .filter { abs(it.netBalance) > 0.01 }
            .map {
                OpenBalanceItem(
                    friendId = it.friend.id,
                    friendName = it.friend.name,
                    amount = it.netBalance,
                    contact = it.friend.contact
                )
            }
            .sortedByDescending { abs(it.amount) }

        SharedTabsUiState(
            comingToYou = comingToYou,
            youStillOwe = youStillOwe,
            netBalance = netBalance,
            settledPercent = settledPercent,
            friendsWithBalances = friendsWithBalances,
            openBalances = openBalances,
            recentSplits = allSplits,
            allFriends = friends
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharedTabsUiState()
    )

    // My Money UI State Flow
    val myMoneyState: StateFlow<MyMoneyUiState> = combine(
        repository.monthlyIncomeFlow,
        repository.allExpenses
    ) { income, expenses ->
        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: "Month"
        val year = cal.get(Calendar.YEAR)
        val monthYearStr = "$monthName $year"

        val spent = expenses.sumOf { it.amount }
        val left = income - spent
        val percentUsed = if (income > 0) ((spent / income) * 100).toInt().coerceIn(0, 100) else 0

        val daysRemaining = max(1, daysInMonth - currentDay)
        val dailyBudget = if (daysRemaining > 0) max(0.0, left / daysRemaining) else 0.0

        val expectedSpendByNow = (income / daysInMonth) * currentDay
        val paceMessage = if (spent <= expectedSpendByNow) {
            "On pace. ₹${"%,d".format(left.toLong())} left with $daysRemaining days to go — that is about ₹${"%,d".format(dailyBudget.toLong())} a day."
        } else {
            "Pacing fast. ₹${"%,d".format(left.toLong())} left with $daysRemaining days to go — target ₹${"%,d".format(dailyBudget.toLong())} a day."
        }

        // Radar data for categories: Food, Travel, Rent, Study, Fun, Other
        val categories = listOf("Food", "Travel", "Rent", "Study", "Fun", "Other")
        val colorMap = mapOf(
            "Food" to CategoryFood,
            "Travel" to CategoryTravel,
            "Rent" to CategoryRent,
            "Study" to CategoryStudy,
            "Fun" to CategoryFun,
            "Other" to CategoryOther
        )

        val radarList = categories.map { cat ->
            val catTotal = expenses.filter { it.category.equals(cat, ignoreCase = true) }.sumOf { it.amount }
            RadarCategoryData(
                category = cat,
                amount = catTotal,
                color = colorMap[cat] ?: CategoryOther
            )
        }

        val topCategory = radarList.maxByOrNull { it.amount }
        val topCategoryHighlight = if (topCategory != null && topCategory.amount > 0) {
            "${topCategory.category} leads"
        } else {
            "No expenses yet"
        }

        MyMoneyUiState(
            currentMonthYear = monthYearStr,
            income = income,
            spent = spent,
            left = left,
            percentUsed = percentUsed,
            currentDay = currentDay,
            daysInMonth = daysInMonth,
            daysRemaining = daysRemaining,
            dailyBudget = dailyBudget,
            paceMessage = paceMessage,
            radarData = radarList,
            topCategoryHighlight = topCategoryHighlight,
            expenses = expenses
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MyMoneyUiState()
    )

    // Action methods
    fun logSplit(friendId: Long, friendName: String, title: String, amount: Double, isOwedToUser: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSplit(
                SplitEntity(
                    friendId = friendId,
                    friendName = friendName,
                    title = title,
                    amount = amount,
                    isOwedToUser = isOwedToUser,
                    isSettled = false
                )
            )
        }
    }

    fun toggleSplitSettled(splitId: Long, settled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleSplitSettled(splitId, settled)
        }
    }

    fun deleteSplit(splitId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSplit(splitId)
        }
    }

    fun updateSplit(split: SplitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSplit(split)
        }
    }

    fun addFriend(name: String, contact: String, colorHex: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addFriend(name, contact, colorHex)
        }
    }

    fun updateFriend(friend: FriendEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFriend(friend)
        }
    }

    fun deleteFriend(friendId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFriend(friendId)
        }
    }

    fun logExpense(title: String, amount: Double, category: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addExpense(
                ExpenseEntity(
                    title = title,
                    amount = amount,
                    category = category,
                    notes = notes
                )
            )
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(id)
        }
    }

    fun setMonthlyIncome(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setMonthlyIncome(amount)
        }
    }

    // Savings Coach Query Engine
    fun askCoach(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return

        _isCoachThinking.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val shared = sharedTabsState.value
            val money = myMoneyState.value

            val responseText = generateHeuristicResponse(trimmed, shared, money)

            val newMsg = CoachMessage(
                query = trimmed,
                response = responseText,
                isOnline = _isOnlineCoachMode.value
            )

            _coachMessages.value = _coachMessages.value + newMsg
            _isCoachThinking.value = false
        }
    }

    private fun generateHeuristicResponse(
        prompt: String,
        shared: SharedTabsUiState,
        money: MyMoneyUiState
    ): String {
        val lower = prompt.lowercase()

        return when {
            lower.contains("month") && (lower.contains("going") || lower.contains("how")) -> {
                "You are currently on track! You have spent ₹${"%,d".format(money.spent.toInt())} out of your ₹${"%,d".format(money.income.toInt())} income (${money.percentUsed}% used). With ${money.daysRemaining} days left in ${money.currentMonthYear.substringBefore(" ")}, you have about ₹${"%,d".format(money.dailyBudget.toInt())} to spend per day."
            }

            lower.contains("cut back") || lower.contains("reduce") || lower.contains("save more") -> {
                val topCat = money.radarData.maxByOrNull { it.amount }
                val topCatName = topCat?.category ?: "discretionary"
                val topCatAmt = topCat?.amount?.toInt() ?: 0
                "Your highest outflow category is $topCatName at ₹${"%,d".format(topCatAmt)}. Since fixed costs like Rent are hard to shift quickly, trimming your Food and Fun categories by 15% can easily unlock ~₹1,200 in monthly buffer."
            }

            lower.contains("overspent") || lower.contains("what now") -> {
                val pendingOwed = shared.comingToYou.toInt()
                "Don't panic! Adjust your remaining daily allowance to ₹${"%,d".format(money.dailyBudget.toInt())}/day. Also, note that friends currently owe you ₹${"%,d".format(pendingOwed)} across your shared tabs — collecting those will immediately soften the impact."
            }

            lower.contains("10,000") || lower.contains("10000") || (lower.contains("save") && lower.contains("how")) -> {
                val monthlySavings = max(0.0, money.income - money.spent)
                if (monthlySavings >= 10000.0) {
                    "Great news: At your current savings rate of ₹${"%,d".format(monthlySavings.toInt())}/month, you will reach ₹10,000 in under 1 month!"
                } else if (monthlySavings > 0) {
                    val monthsNeeded = kotlin.math.ceil(10000.0 / monthlySavings).toInt()
                    "At your current pace of ₹${"%,d".format(monthlySavings.toInt())} saved each month, you'll hit ₹10,000 in approximately $monthsNeeded months."
                } else {
                    "Currently expenses match or exceed income. Setting a daily spend cap of ₹${"%,d".format(money.dailyBudget.toInt())} will generate ₹10,000 within 2-3 months."
                }
            }

            lower.contains("chase") || lower.contains("owe me") || lower.contains("who") -> {
                val debtors = shared.openBalances.filter { it.amount > 0 }
                if (debtors.isNotEmpty()) {
                    val listStr = debtors.joinToString { "${it.friendName} (₹${"%,d".format(it.amount.toInt())})" }
                    "You have ₹${"%,d".format(shared.comingToYou.toInt())} in open tabs coming to you from: $listStr. Sending a gentle split reminder today is recommended!"
                } else {
                    "All your friends have settled up their tabs with you. No one owes you right now!"
                }
            }

            lower.contains("split") || lower.contains("tab") -> {
                "In your shared tabs, you are owed ₹${"%,d".format(shared.comingToYou.toInt())} and you owe ₹${"%,d".format(shared.youStillOwe.toInt())}. Your net position is ${if (shared.netBalance >= 0) "+₹" else "-₹"}${"%,d".format(abs(shared.netBalance).toInt())}."
            }

            else -> {
                "Based on your current ledger data: You have ₹${"%,d".format(money.left.toInt())} remaining this month with a daily pace of ₹${"%,d".format(money.dailyBudget.toInt())}/day. In shared tabs, your net balance is ${if (shared.netBalance >= 0) "+₹" else "-₹"}${"%,d".format(abs(shared.netBalance).toInt())}."
            }
        }
    }
}
