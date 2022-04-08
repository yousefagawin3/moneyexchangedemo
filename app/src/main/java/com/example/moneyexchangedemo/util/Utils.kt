package com.example.moneyexchangedemo.util

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_LONG,
    view: View
) {
    Snackbar.make(view, message, duration).show()
}