package com.example.moneyexchangedemo.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserBalance::class, Commission::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userBalanceDao() : UserBalanceDao
}