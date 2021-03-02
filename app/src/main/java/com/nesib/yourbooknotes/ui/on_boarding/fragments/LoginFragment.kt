package com.nesib.yourbooknotes.ui.on_boarding.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentLoginBinding
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        subscribeObserver()
        setupClickListeners()
    }


    private fun setupClickListeners() {
        binding.loginBtn.setOnClickListener {
            authViewModel.login(
                binding.emailEditText.text.toString(),
                binding.passwordEditText.text.toString()
            )
        }
        binding.loginToSignupBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }

    private fun subscribeObserver() {
        authViewModel.auth.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    binding.loginBtnProgressBar.visibility = View.GONE
                    binding.loginBtnTextView.visibility = View.VISIBLE
                    findNavController().navigate(R.id.action_loginFragment_to_selectCategoriesFragment)
                }
                is DataState.Fail -> {
                    binding.loginBtnProgressBar.visibility = View.GONE
                    binding.loginBtnTextView.visibility = View.VISIBLE
                }
                is DataState.Loading -> {
                    binding.loginBtnProgressBar.visibility = View.VISIBLE
                    binding.loginBtnTextView.visibility = View.GONE
                }
            }
        }
    }
}