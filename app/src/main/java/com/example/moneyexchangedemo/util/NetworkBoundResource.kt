package com.example.moneyexchangedemo.util

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

inline fun <RequestType>
        networkBoundResource(
    crossinline fetch: suspend ()                       //this is responsible for fetching new data from the API
    -> RequestType,
    crossinline saveFetchResult: suspend (RequestType?)  //this func is responsible to store what is being fetched from the API; we declare the return type as (Unit) because it doesn't return anything
    -> Unit,
    crossinline onAPISuccess: (RequestType?)                      //this func doesn't take any input and doesn't take anything
    -> Unit = { },                                      //we make it optional by making it a Unit and pass nothing
    crossinline onAPIFailed: (Throwable)              //this func that takes in a Throwable so we can show the error in the UI
    -> Unit = { }                                       //this func doesn't return anything
) =
    channelFlow {                                       //we use channelFlow instead of normal flow, bec channelFlow can execute work concurrently

//    LogUtil.e("networkBoundResource","1")
        val loading = launch {
            send(Resource.Loading<RequestType>(null))
        }
        //we put try-catch bec there might be some problem when fetching data during the process inside this block
        try {
            //we save the data that is coming from (fetch())
            saveFetchResult(fetch())
            //we declare that we are done fetching
            onAPISuccess(fetch())
            //we cancel the loading once we are successful in fetching data
            loading.cancel()
        } catch (t: Throwable) {
            onAPIFailed(t)
            //we cancel loading if something went wrong
            loading.cancel()
        }
    }