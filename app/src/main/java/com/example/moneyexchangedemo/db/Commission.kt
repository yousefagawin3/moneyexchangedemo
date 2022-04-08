package com.example.moneyexchangedemo.db

import androidx.room.Entity
import androidx.room.PrimaryKey

//for this scope we only want to store one user and track its very own commission rate
const val USER_COMMISSION_RATE_ID = 0

@Entity(tableName = "commission_rate")
data class Commission(
    @PrimaryKey val id: Int = USER_COMMISSION_RATE_ID,
    val transactions_done: Int
)
