package com.nesib.yourbooknotes.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.nesib.yourbooknotes.R


class SpinnerAdapter(context: Context, genres: List<String>) :
    ArrayAdapter<String>(context, R.layout.spinner_item_layout, genres) {
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val spinnerItemView = super.getDropDownView(position, null, parent) as TextView
        spinnerItemView.setBackgroundResource(R.drawable.light_gray_border_bottom_bg)

        if (position == 0) {
            spinnerItemView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorSecondaryText
                )
            )
        } else {
            spinnerItemView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryText))
        }
        return spinnerItemView
    }

    override fun isEnabled(position: Int) = position != 0

}