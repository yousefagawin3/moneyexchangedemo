package com.example.moneyexchangedemo.db

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBalanceDao {

    //this function will UPdate or inSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)    //this will ensure that you override the newly inserted one in case of conflict
    suspend fun upsertCommissionRate(commission: Commission)

    @Query("SELECT * FROM commission_rate WHERE id = $USER_COMMISSION_RATE_ID")
    fun getCommissionRate() : LiveData<Commission>

    @Query("SELECT * FROM user_balance")
    fun getAllUserBalance() : Flow<List<UserBalance>>

    @Query("SELECT * FROM user_balance WHERE currency = :currency")
    fun getUserBalanceOnCurrency(currency : String) : Flow<UserBalance?>

    @Query("UPDATE user_balance SET amount = amount + :amount WHERE currency = :currency")
    suspend fun addToUserBalance(currency: String, amount: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserBalance(userBalance: UserBalance) : Long

    @Query("INSERT OR IGNORE INTO user_balance (currency, amount) VALUES (:currency, :amount)")
    suspend fun insertOrIgnoreUserBalance(currency: String, amount: Double) : Long

    @Transaction
    suspend fun insertOrAddUserBalance(currency: String, amount: Double){
        val result = insertOrIgnoreUserBalance(currency, amount)
        if(result < 0L){
            addToUserBalance(currency, amount)
        }
    }
}