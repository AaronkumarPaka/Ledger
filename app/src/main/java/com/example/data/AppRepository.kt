package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppRepository(
    private val expenseDao: ExpenseDao,
    private val friendDao: FriendDao,
    private val splitDao: SplitDao,
    private val userSettingsDao: UserSettingsDao,
    private val database: AppDatabase
) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allFriends: Flow<List<FriendEntity>> = friendDao.getAllFriends()
    val allSplits: Flow<List<SplitEntity>> = splitDao.getAllSplits()
    val recentSplits: Flow<List<SplitEntity>> = splitDao.getRecentSplits()

    val monthlyIncomeFlow: Flow<Double> = userSettingsDao.getSettingFlow("monthly_income")
        .map { it?.toDoubleOrNull() ?: 18000.0 }

    suspend fun addExpense(expense: ExpenseEntity) = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(id: Long) = expenseDao.deleteExpenseById(id)

    suspend fun addFriend(name: String, contact: String, colorHex: Long): Long {
        return friendDao.insertFriend(
            FriendEntity(name = name, contact = contact, avatarColorHex = colorHex)
        )
    }

    suspend fun updateFriend(friend: FriendEntity) {
        friendDao.updateFriend(friend)
        // Keep split records updated if name changed
        splitDao.updateFriendNameInSplits(friend.id, friend.name)
    }

    suspend fun deleteFriend(id: Long, deleteAssociatedSplits: Boolean = true) {
        if (deleteAssociatedSplits) {
            splitDao.deleteSplitsByFriendId(id)
        }
        friendDao.deleteFriendById(id)
    }

    suspend fun addSplit(split: SplitEntity) = splitDao.insertSplit(split)

    suspend fun updateSplit(split: SplitEntity) = splitDao.updateSplit(split)

    suspend fun toggleSplitSettled(id: Long, settled: Boolean) = splitDao.setSettled(id, settled)

    suspend fun deleteSplit(id: Long) = splitDao.deleteSplitById(id)

    suspend fun setMonthlyIncome(amount: Double) {
        userSettingsDao.saveSetting(UserSettingsEntity("monthly_income", amount.toString()))
    }

    suspend fun seedIfEmpty() {
        AppDatabase.populateInitialData(database)
    }
}
