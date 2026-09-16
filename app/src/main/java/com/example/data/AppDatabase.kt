package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExpenseEntity::class,
        FriendEntity::class,
        SplitEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun friendDao(): FriendDao
    abstract fun splitDao(): SplitDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ledgerlink_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val expenseDao = database.expenseDao()
            val friendDao = database.friendDao()
            val splitDao = database.splitDao()
            val settingsDao = database.userSettingsDao()

            if (settingsDao.getSetting("monthly_income") == null) {
                settingsDao.saveSetting(UserSettingsEntity("monthly_income", "18000.0"))
            }

            if (friendDao.getCount() == 0) {
                val rohanId = friendDao.insertFriend(
                    FriendEntity(name = "Rohan Sharma", contact = "+91 98765 43210", avatarColorHex = 0xFF3B82F6)
                )
                val priyaId = friendDao.insertFriend(
                    FriendEntity(name = "Priya Patel", contact = "priya.p@email.com", avatarColorHex = 0xFFEC4899)
                )
                val ananyaId = friendDao.insertFriend(
                    FriendEntity(name = "Ananya Sen", contact = "+91 91234 56789", avatarColorHex = 0xFF10B981)
                )
                val vikramId = friendDao.insertFriend(
                    FriendEntity(name = "Vikram Malhotra", contact = "vikram.m@domain.in", avatarColorHex = 0xFFF59E0B)
                )

                val now = System.currentTimeMillis()
                val oneDay = 24L * 60 * 60 * 1000

                // Splits configuration matching exact specs:
                // Coming to you: ₹855 (Rohan ₹580 + Ananya ₹275)
                // You still owe: ₹1,250 (Priya ₹1,250)
                // Net balance: -₹395
                // Settled items: ~17% settled percentage
                val splits = listOf(
                    SplitEntity(
                        friendId = rohanId,
                        friendName = "Rohan Sharma",
                        title = "Dinner at Social",
                        amount = 580.0,
                        isOwedToUser = true,
                        isSettled = false,
                        timestamp = now - (oneDay * 1)
                    ),
                    SplitEntity(
                        friendId = priyaId,
                        friendName = "Priya Patel",
                        title = "Flat Wi-Fi & Electricity bill",
                        amount = 1250.0,
                        isOwedToUser = false,
                        isSettled = false,
                        timestamp = now - (oneDay * 2)
                    ),
                    SplitEntity(
                        friendId = ananyaId,
                        friendName = "Ananya Sen",
                        title = "Uber to Terminal 2",
                        amount = 275.0,
                        isOwedToUser = true,
                        isSettled = false,
                        timestamp = now - (oneDay * 3)
                    ),
                    SplitEntity(
                        friendId = vikramId,
                        friendName = "Vikram Malhotra",
                        title = "IMAX Tickets (Oppenheimer)",
                        amount = 450.0,
                        isOwedToUser = true,
                        isSettled = true,
                        timestamp = now - (oneDay * 5)
                    ),
                    SplitEntity(
                        friendId = rohanId,
                        friendName = "Rohan Sharma",
                        title = "Specialty Coffee Beans",
                        amount = 220.0,
                        isOwedToUser = true,
                        isSettled = true,
                        timestamp = now - (oneDay * 7)
                    )
                )
                splitDao.insertAll(splits)
            }

            if (expenseDao.getCount() == 0) {
                val now = System.currentTimeMillis()
                val oneDay = 24L * 60 * 60 * 1000
                // Total Spent = 3500 + 1250 + 450 + 620 + 500 = 6,320
                // Left = 18000 - 6320 = 11,680
                val sampleExpenses = listOf(
                    ExpenseEntity(
                        title = "Apartment Rent & Maintenance",
                        amount = 3500.0,
                        category = "Rent",
                        timestamp = now - (oneDay * 10),
                        notes = "Monthly flat share share"
                    ),
                    ExpenseEntity(
                        title = "Supermarket Provisions",
                        amount = 1250.0,
                        category = "Food",
                        timestamp = now - (oneDay * 4),
                        notes = "Fresh greens, grains, olive oil"
                    ),
                    ExpenseEntity(
                        title = "Data Science Textbook & Course",
                        amount = 620.0,
                        category = "Study",
                        timestamp = now - (oneDay * 3),
                        notes = "Machine learning foundations"
                    ),
                    ExpenseEntity(
                        title = "Acoustic Live Concert Ticket",
                        amount = 500.0,
                        category = "Fun",
                        timestamp = now - (oneDay * 2),
                        notes = "Weekend evening live show"
                    ),
                    ExpenseEntity(
                        title = "Metro SmartCard Auto-Recharge",
                        amount = 450.0,
                        category = "Travel",
                        timestamp = now - (oneDay * 1),
                        notes = "Daily commute transit pass"
                    )
                )
                expenseDao.insertAll(sampleExpenses)
            }
        }
    }
}
