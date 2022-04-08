package com.example.moneyexchangedemo.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CurrencyRate(
    var currency : String,
    var value: Double
) : Parcelable
