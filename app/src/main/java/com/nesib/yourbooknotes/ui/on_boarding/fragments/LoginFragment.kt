package com.nesib.yourbooknotes.ui.on_boarding.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentLoginBinding
import com.nesib.yourbooknotes.ui.on_boarding.StartActivity
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var authViewModel:AuthViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

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
                    binding.loginErrorTextView.visibility = View.INVISIBLE
                    findNavController().navigate(R.id.action_loginFragment_to_selectCategoriesFragment)
                }
                is DataState.Fail -> {
                    if(authViewModel.hasLoginError){
                        binding.loginErrorTextView.visibility = View.VISIBLE
                        binding.loginErrorTextView.text = it.message
                        binding.loginBtn.isEnabled = true
                        binding.loginBtnProgressBar.visibility = View.GONE
                        binding.loginBtnTextView.visibility = View.VISIBLE
                    }
                }
                is DataState.Loading -> {
                    binding.loginBtn.isEnabled = false
                    binding.loginBtnProgressBar.visibility = View.VISIBLE
                    binding.loginBtnTextView.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.hasLoginError = false
    }
}