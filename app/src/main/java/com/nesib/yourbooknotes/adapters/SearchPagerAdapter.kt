package com.nesib.yourbooknotes.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nesib.yourbooknotes.ui.main.fragments.search.SearchBooksFragment
import com.nesib.yourbooknotes.ui.main.fragments.search.SelectGenresFragment
import com.nesib.yourbooknotes.ui.main.fragments.search.SearchUsersFragment

class SearchPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val fragments = listOf(SelectGenresFragment(),SearchBooksFragment(), SearchUsersFragment())
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}