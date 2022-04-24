package com.nesib.quotegram.ui.on_boarding.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.SplashPagerAdapter
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentSplashBinding
import com.nesib.quotegram.ui.on_boarding.StartActivity
import com.nesib.quotegram.utils.Constants
import com.nesib.quotegram.utils.gone
import com.nesib.quotegram.utils.visible

class SplashFragment : BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        directToLoginOrSignup()
        setupClickListeners()
        setupViewPager()
        (requireActivity() as StartActivity).hideToolbar()
    }

    private fun setupClickListeners() {
        binding.nextButton.setOnClickListener {
            binding.splashViewPager.currentItem += 1
        }

        binding.skipButton.setOnClickListener {
            (requireActivity() as StartActivity).showToolbar()
            val action =
                SplashFragmentDirections.actionSplashFragmentToSelectCategoriesFragment(null, null)
            findNavController().navigate(action)
        }
    }


    private fun setupViewPager() = with(binding) {
        val adapter = SplashPagerAdapter()
        splashViewPager.adapter = adapter
        splashViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == adapter.itemCount - 1) {
                    nextButton.setOnClickListener {
                        (requireActivity() as StartActivity).showToolbar()
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                    nextButtonTextView.text = getString(R.string.txt_get_started)
                    listOf(spacer, skipButton).visible()
                } else {
                    nextButtonTextView.text = getString(R.string.txt_next)
                    nextButton.setOnClickListener {
                        splashViewPager.currentItem += 1
                    }
                    listOf(spacer, skipButton).gone()
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
        val directToLogin =
            requireActivity().intent.getBooleanExtra(Constants.TEXT_DIRECT_TO_LOGIN, false)
        val directToSignup =
            requireActivity().intent.getBooleanExtra(Constants.TEXT_DIRECT_TO_SIGNUP, false)
        if (directToLogin) {
            requireActivity().intent = Intent()
            findNavController().navigate(R.id.loginFragment)
        } else if (directToSignup) {
            requireActivity().intent = Intent()
            findNavController().navigate(R.id.signupFragment)
        }
    }

    override fun createBinding(view: View) = FragmentSplashBinding.bind(view)
}