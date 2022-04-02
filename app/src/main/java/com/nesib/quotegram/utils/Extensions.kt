package com.nesib.quotegram.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.*

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(message: String) {
    Toast.makeText(this.requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun String.isEmail(): Boolean {
    return this.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isPassword(): Boolean {
    return this.length >= 5
}

fun String.isUsername(): Boolean {
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

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun List<View>.show() {
    this.forEach {
        it.show()
    }
}

fun List<View>.hide() {
    this.forEach {
        it.hide()
    }
}

fun List<View>.gone() {
    this.forEach {
        it.gone()
    }
}