package com.nesib.yourbooknotes.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Context.showToast(message: String) {
    Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(message: String) {
    Toast.makeText(this.requireContext(),message,Toast.LENGTH_SHORT).show()
}