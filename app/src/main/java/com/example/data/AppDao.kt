package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getCount(): Int
}

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY name ASC")
    fun getAllFriends(): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(friends: List<FriendEntity>)

    @Update
    suspend fun updateFriend(friend: FriendEntity)

    @Delete
    suspend fun deleteFriend(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE id = :id")
    suspend fun deleteFriendById(id: Long)

    @Query("SELECT COUNT(*) FROM friends")
    suspend fun getCount(): Int
}

@Dao
interface SplitDao {
    @Query("SELECT * FROM splits ORDER BY timestamp DESC")
    fun getAllSplits(): Flow<List<SplitEntity>>

    @Query("SELECT * FROM splits ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSplits(): Flow<List<SplitEntity>>

    @Query("SELECT * FROM splits WHERE friendId = :friendId ORDER BY timestamp DESC")
    fun getSplitsForFriend(friendId: Long): Flow<List<SplitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: SplitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(splits: List<SplitEntity>)

    @Update
    suspend fun updateSplit(split: SplitEntity)

    @Query("UPDATE splits SET isSettled = :settled WHERE id = :id")
    suspend fun setSettled(id: Long, settled: Boolean)

    @Query("DELETE FROM splits WHERE id = :id")
    suspend fun deleteSplitById(id: Long)

    @Query("DELETE FROM splits WHERE friendId = :friendId")
    suspend fun deleteSplitsByFriendId(friendId: Long)

    @Query("UPDATE splits SET friendName = :newName WHERE friendId = :friendId")
    suspend fun updateFriendNameInSplits(friendId: Long, newName: String)

    @Query("SELECT COUNT(*) FROM splits")
    suspend fun getCount(): Int
}

@Dao
interface UserSettingsDao {
    @Query("SELECT value FROM user_settings WHERE `key` = :key")
    fun getSettingFlow(key: String): Flow<String?>

    @Query("SELECT value FROM user_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: UserSettingsEntity)
}
