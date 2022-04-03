package com.nesib.quotegram.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

object KeyboardUtils {
    private var showing = false
    val isShowing
        get() = showing

    fun toggleKeyboard(isVisible: Boolean, view: View, context: Context) {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (isVisible) {
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            showing = true
        } else {
            inputManager.hideSoftInputFromWindow(view.windowToken, 0)
            showing = false
        }
    }


}