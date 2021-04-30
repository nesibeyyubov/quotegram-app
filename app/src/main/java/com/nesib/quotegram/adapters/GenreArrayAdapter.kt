package com.nesib.quotegram.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.nesib.quotegram.R

class GenreArrayAdapter(context: Context, val genres: Array<String>) :
    ArrayAdapter<String>(context, 0, genres) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position, convertView, parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, true)
        }
        view!!.findViewById<TextView>(R.id.item_textView).text = genres[position]
        return view
    }
}
