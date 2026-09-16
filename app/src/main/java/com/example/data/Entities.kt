package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // Food, Travel, Rent, Study, Fun, Other
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val contact: String,
    val avatarColorHex: Long = 0xFF3B82F6
)

@Entity(tableName = "splits")
data class SplitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val friendId: Long,
    val friendName: String,
    val title: String,
    val amount: Double,
    val isOwedToUser: Boolean, // true: Friend owes user ("coming to you"), false: User owes friend ("you owe")
    val isSettled: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
