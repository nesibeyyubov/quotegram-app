package com.nesib.quotegram.adapters

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nesib.quotegram.ui.on_boarding.fragments.splash.FirstFragment
import com.nesib.quotegram.ui.on_boarding.fragments.splash.LastFragment
import com.nesib.quotegram.ui.on_boarding.fragments.splash.SecondFragment

class SplashPagerAdapter(activity:FragmentActivity) : FragmentStateAdapter(activity) {
    val fragments = listOf(FirstFragment(),SecondFragment(),LastFragment())
    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}
