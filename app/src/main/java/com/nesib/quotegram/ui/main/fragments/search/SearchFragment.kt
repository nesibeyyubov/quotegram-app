package com.nesib.quotegram.ui.main.fragments.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.SearchPagerAdapter
import com.nesib.quotegram.databinding.FragmentSearchBinding
import com.nesib.quotegram.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var pagerAdapter: SearchPagerAdapter
    private val sharedViewModel:SharedViewModel by viewModels({requireActivity()})
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        setupTabLayout()
    }


    private fun setupTabLayout() {
        pagerAdapter = SearchPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sharedViewModel.currentTabIndex = position
            }
        })
        TabLayoutMediator(
            binding.searchTabLayout,
            binding.viewPager
        ) { tab: TabLayout.Tab, i: Int ->
            tab.text = when (i) {
                0 -> "Quotes"
                1 -> "Books"
                2 -> "Users"
                else -> ""
            }
        }.attach()
    }


}