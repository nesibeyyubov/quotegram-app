package com.nesib.quotegram.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import com.nesib.quotegram.R
import java.util.*

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(message: String) {
    Toast.makeText(this.requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun NavDestination.isRootScreen(): Boolean {
    return when (id) {
        in Constants.ROOT_SCREENS -> true
        else -> false
    }
}

fun NavDestination.shouldShowBottomNav(): Boolean {
    if (isRootScreen()) return true
    return when (id) {
        R.id.searchQuotesFragment, R.id.quoteOptionsFragment, R.id.userProfileFragment, R.id.quoteFragment -> true
        else -> false
    }
}


fun String.isEmail(): Boolean {
    return this.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= 5
}

fun String.isValidUsername(): Boolean {
    return this.length >= 5
}

fun Int.toFormattedNumber(): String {
    var text = "$this"
    if (this in 1000..9999) {
        text = (this / 1000).toString() + "." + ((this / 100) % 10).toString() + "k"
    } else if (this in 10000..99999) {
        text = (this / 1000).toString() + "." + ((this / 100) % 10).toString() + "k"
    }
    return text
}

fun List<String>.toJoinedString(): String {
    var index = 0
    var genresText = ""
    this.forEach { genre ->
        if (index < this.size - 1) {
            genresText += "${genre.toLowerCase(Locale.ROOT)},"
        } else {
            genresText += genre.toLowerCase(Locale.ROOT)
        }
        index++
    }
    return genresText
}

fun toggleKeyboard(isVisible: Boolean, view: View, context: Context) {
    val inputManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (isVisible) {
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    } else {
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun EditText.focusWithKeyboard() {
    if (this.requestFocus()) {
        toggleKeyboard(isVisible = true, this, context)
    }
}

fun TextView.showError(message: String) {
    this.visibility = View.VISIBLE
    this.text = message
}

fun EditText.unFocus() {
    toggleKeyboard(isVisible = false, this, context)
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.safeInvisible() {
    if (isVisible()) {
        this.invisible()
    }
}

fun View.safeVisible() {
    if (!isVisible()) {
        this.visible()
    }
}

fun View.safeGone() {
    if (isVisible()) {
        this.gone()
    }
}

fun List<View>.visible() {
    this.forEach {
        it.visible()
    }
}

fun List<View>.invisible() {
    this.forEach {
        it.invisible()
    }
}

fun List<View>.gone() {
    this.forEach {
        it.gone()
    }
}

fun View.isVisible() = this.visibility == View.VISIBLE