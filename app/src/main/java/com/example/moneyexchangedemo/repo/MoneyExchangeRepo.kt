package com.example.moneyexchangedemo.repo

import com.example.moneyexchangedemo.BuildConfig
import com.example.moneyexchangedemo.db.AppDatabase
import com.example.moneyexchangedemo.db.Commission
import com.example.moneyexchangedemo.db.UserBalance
import com.example.moneyexchangedemo.model.APIResponse
import com.example.moneyexchangedemo.network.MoneyExchangeAPI
import com.example.moneyexchangedemo.util.Resource
import com.example.moneyexchangedemo.util.networkBoundResource
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MoneyExchangeRepo @Inject constructor(
    private val moneyExchangeAPI: MoneyExchangeAPI,
    private val appDatabase: AppDatabase
) {
    private val userBalanceDao = appDatabase.userBalanceDao()

    fun getAllRates(
        base: String,
        onAPISuccess: (APIResponse) -> Unit,
        onAPIFailed: (Throwable) -> Unit
    ): Flow<Resource<APIResponse>> =
        networkBoundResource(
            fetch = {
                val response = moneyExchangeAPI.getRates(
                    BuildConfig.EXCHANGE_RATES_API_KEY,
                    base,
                )
                response
            },
            saveFetchResult = {


            },
            onAPISuccess = { apiResponse ->
                if(apiResponse?.success == true) {
                    onAPISuccess(apiResponse)
                } else {
                    onAPIFailed(
                        Throwable(message = apiResponse?.error?.message)
                    )
                }
            }, //we just simply want to forward onFetchSuccess to the getBreakingNews and let the viewmodel using it to know that it succeeded
            onAPIFailed = { t ->
                //internet related(HttpException) or io related(IOException) errors will just let the app crash
                if (t !is HttpException && t !is IOException) {
                    throw t
                }
                //this forward the t from the networkBoundResource going to the viewmodel that calls getBreakingNews
                onAPIFailed(t)
            }
        )

    fun getAllUserBalance() = userBalanceDao.getAllUserBalance()

    suspend fun setUserBalance(userBalance: UserBalance) = userBalanceDao.upsertUserBalance(userBalance)

    fun getCommissionRate() = userBalanceDao.getCommissionRate()

    suspend fun upsertCommissionRate(commission: Commission) = userBalanceDao.upsertCommissionRate(commission)

    //if the insert was unsuccessful meaning there is an existing data already, therefore we just add the value
    suspend fun insertOrAddUserBalance(currency: String, amount: Double) = userBalanceDao.insertOrAddUserBalance(currency, amount)
//    suspend fun addOrInsertUserBalance(currency: String, amount: Double)  {
//        if (userBalanceDao.upsertUserBalance(UserBalance(currency, amount)) != 1L) {
//            userBalanceDao.addToUserBalance(currency, amount)
//        }
//    }



}