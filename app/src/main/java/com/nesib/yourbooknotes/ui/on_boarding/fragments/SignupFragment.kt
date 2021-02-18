package com.nesib.yourbooknotes.ui.on_boarding.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentLoginBinding
import com.nesib.yourbooknotes.databinding.FragmentSignupBinding

class SignupFragment : Fragment(R.layout.fragment_signup) {
    private lateinit var binding: FragmentSignupBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignupBinding.bind(view)

        binding.signupBtn.setOnClickListener{
            findNavController().navigate(R.id.action_signupFragment_to_selectCategoriesFragment)
        }
        binding.signupToLoginBtn.setOnClickListener {
            // Check if it is coming from login then pop back
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }
}