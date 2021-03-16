package com.nesib.yourbooknotes.utils

import android.app.AlertDialog
import android.content.Context
import com.nesib.yourbooknotes.R

fun Context.showNoAuthDialog(){
    val dialog = AlertDialog.Builder(this)
        .setView(R.layout.not_authenticated_layout)
        .create()
    dialog.show()
}