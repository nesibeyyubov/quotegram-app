package com.nesib.yourbooknotes.ui.on_boarding.fragments.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentLastBinding

class LastFragment : Fragment(R.layout.fragment_last) {
    private lateinit var binding:FragmentLastBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLastBinding.bind(view)
        binding.getStartedBtn.setOnClickListener{
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_splashFragment_to_selectCategoriesFragment)
        }
    }
}