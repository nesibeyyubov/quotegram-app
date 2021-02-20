package com.nesib.yourbooknotes.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.nesib.yourbooknotes.R

class GenreArrayAdapter(context: Context,genres: Array<String>) :
    ArrayAdapter<String>(context,0, genres) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Log.d("mytag", "getView: ")
        return initView(position,convertView,parent)
    }


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return initView(position,convertView,parent)
    }

    private fun initView(position: Int, convertView: View?, parent: ViewGroup):View{
        var view = convertView
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.spinner_item,parent,false)
        }
        view!!.findViewById<TextView>(R.id.item_textView).text = getItem(position) ?: ""
        return view
    }
}
