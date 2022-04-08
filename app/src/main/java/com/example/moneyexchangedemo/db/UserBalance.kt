package com.example.moneyexchangedemo.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_balance")
data class UserBalance(
    @PrimaryKey val currency: String,
    val amount: Double
)
