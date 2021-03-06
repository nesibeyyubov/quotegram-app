package com.nesib.yourbooknotes.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nesib.yourbooknotes.ui.main.fragments.SearchBooksFragment
import com.nesib.yourbooknotes.ui.main.fragments.SearchUsersFragment

class SearchPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val fragments = listOf(SearchBooksFragment(),SearchUsersFragment())
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}