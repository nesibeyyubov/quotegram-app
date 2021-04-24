package com.nesib.yourbooknotes.ui.on_boarding.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.SplashPagerAdapter
import com.nesib.yourbooknotes.databinding.FragmentSplashBinding

class SplashFragment : Fragment(R.layout.fragment_splash) {
    private lateinit var binding: FragmentSplashBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSplashBinding.bind(view)
        directToLoginOrSignup()
        setupClickListeners()
        setupViewPager()

    }

    private fun setupClickListeners() {
        binding.prevBtn.setOnClickListener {
            val currentIndex = binding.splashViewPager.currentItem
            binding.splashViewPager.currentItem = currentIndex - 1
        }
        binding.nextBtn.setOnClickListener {
            val currentIndex = binding.splashViewPager.currentItem
            binding.splashViewPager.currentItem = currentIndex + 1
        }
    }

    private fun setupViewPager() {
        val adapter = SplashPagerAdapter(requireActivity())
        binding.splashViewPager.adapter = adapter
        binding.splashViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position + 1 == adapter.fragments.size) {
                    binding.nextBtn.visibility = View.INVISIBLE
                }
                if (position < adapter.fragments.size - 1) {
                    binding.nextBtn.visibility = View.VISIBLE
                }
                if (position == 0) {
                    binding.prevBtn.visibility = View.INVISIBLE
                } else if (position > 0) {
                    binding.prevBtn.visibility = View.VISIBLE
                }
            }
        })
        TabLayoutMediator(
            binding.splashTabLayout,
            binding.splashViewPager
        ) { tab: TabLayout.Tab, position: Int ->

        }.attach()
    }

    private fun directToLoginOrSignup() {
        val directToLogin = requireActivity().intent.getBooleanExtra("directToLogin", false)
        val directToSignup = requireActivity().intent.getBooleanExtra("directToSignup", false)
        if (directToLogin) {
            findNavController().navigate(R.id.loginFragment)
        } else if (directToSignup) {
            findNavController().navigate(R.id.signupFragment)
        }
    }
}