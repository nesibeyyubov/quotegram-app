package com.nesib.quotegram.utils

import android.util.Log
import androidx.viewpager2.widget.ViewPager2

class ViewPagerPagination<T>(private val items: List<T>, val listener: (currentPage: Int) -> Unit) :
    ViewPager2.OnPageChangeCallback() {
    private var currentPage = 1
    private var loading = false

    override fun onPageSelected(position: Int) {
        if (position + 1 == items.size) {
            Log.d("mytag", "pagination time: ${position + 1}")
            currentPage++
            listener.invoke(currentPage)
            loading = true
        }
        super.onPageSelected(position)
    }

    fun paginationFinished(){
        loading = false
    }
}