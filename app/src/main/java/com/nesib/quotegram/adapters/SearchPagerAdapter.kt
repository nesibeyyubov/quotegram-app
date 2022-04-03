package com.nesib.quotegram.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nesib.quotegram.ui.main.fragments.search.SelectGenresFragment
import com.nesib.quotegram.ui.main.fragments.search.SearchUsersFragment

class SearchPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val fragments = listOf(SelectGenresFragment(), SearchUsersFragment())
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}