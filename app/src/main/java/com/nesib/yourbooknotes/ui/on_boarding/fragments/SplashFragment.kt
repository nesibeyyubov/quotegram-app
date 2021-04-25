package com.nesib.yourbooknotes.ui.on_boarding.fragments

import android.content.Intent
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
        binding.nextButton.setOnClickListener {
            binding.splashViewPager.currentItem += 1
        }

        binding.skipButton.setOnClickListener {
            val action = SplashFragmentDirections.actionSplashFragmentToSelectCategoriesFragment(null,null)
            findNavController().navigate(action)
        }
    }


    private fun setupViewPager() {
        val adapter = SplashPagerAdapter(requireActivity())
        binding.splashViewPager.adapter = adapter
        binding.splashViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if(position == adapter.fragments.size-1){
                    binding.nextButton.setOnClickListener {
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                    binding.nextButtonTextView.text = "Get Started"
                    binding.spacer.visibility = View.VISIBLE
                    binding.skipButton.visibility = View.VISIBLE
                }else{
                    binding.nextButtonTextView.text = "Next"
                    binding.nextButton.setOnClickListener {
                        binding.splashViewPager.currentItem += 1
                    }
                    binding.spacer.visibility = View.GONE
                    binding.skipButton.visibility = View.GONE
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
            requireActivity().intent = Intent()
            findNavController().navigate(R.id.loginFragment)
        } else if (directToSignup) {
            requireActivity().intent = Intent()
            findNavController().navigate(R.id.signupFragment)
        }
    }
}