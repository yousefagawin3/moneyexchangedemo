package com.example.moneyexchangedemo.model

import org.json.JSONObject

data class APIResponse(
    var success : Boolean = false,
    val base : String?,
    var rates : Map<String, Double>?,
    var error : APIError
)
