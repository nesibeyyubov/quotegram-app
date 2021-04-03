package com.nesib.yourbooknotes.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import java.util.*

fun Context.showToast(message: String) {
    Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(message: String) {
    Toast.makeText(this.requireContext(),message,Toast.LENGTH_SHORT).show()
}

fun List<String>.toJoinedString():String{
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