package com.example.moneyexchangedemo.util

/**
 *
 * by making this class a sealed class, we tell the compiler that the only possible subclasses are the one we put in the below classes (Success, Loading, Error)
 * using sealed class is useful to use in when-statements bec the IDE can automatically add all the different branches for us
 */
sealed class Resource<T>(               //we put <T> to keep this class reusable for all data types
    val data: T? = null,                //we make (data: T?) nullable because the loading state and error state might not contain any data
    val error: Throwable? = null        //in case we have error, we'll put it in (error: Throwable?) and use it in the UI to show the appropriate error message
) {
    class Success<T>(data: T)           //the type of (Resource<T>) will automatically be the type of (data: T) here
        : Resource<T>(data)

    class Loading<T>(data: T? = null)   //the (data: T?) is nullable bec we don't necessarily need to pass data always when it is in loading
        : Resource<T>(data)

    class Error<T>(
        throwable: Throwable,           // (throwable: Throwable) is not nullable bec if something went wrong we can still pass exceptions
        data: T? = null                 // (data: T? = null) is nullable bec we want to decide if we show cache data
    ) : Resource<T>(data, throwable)
}