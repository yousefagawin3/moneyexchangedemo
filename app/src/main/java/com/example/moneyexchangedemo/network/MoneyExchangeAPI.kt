package com.example.moneyexchangedemo.network

import com.example.moneyexchangedemo.model.APIResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MoneyExchangeAPI {

    @GET("latest")
    suspend fun getRates(
        @Query("access_key") access_key: String,
        @Query("base") base: String
    ) : APIResponse

}