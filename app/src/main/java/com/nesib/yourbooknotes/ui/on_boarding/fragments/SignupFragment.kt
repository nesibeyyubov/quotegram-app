package com.nesib.yourbooknotes.ui.on_boarding.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentLoginBinding
import com.nesib.yourbooknotes.databinding.FragmentSignupBinding
import com.nesib.yourbooknotes.ui.on_boarding.StartActivity
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState

class SignupFragment : Fragment(R.layout.fragment_signup) {
    private lateinit var binding: FragmentSignupBinding
    private lateinit var authViewModel:AuthViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignupBinding.bind(view)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        subscribeObserver()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.signupBtn.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            authViewModel.signup(email, password, username, username)
        }
        binding.signupToLoginBtn.setOnClickListener {
            // Check if it is coming from login then pop back
            findNavController().popBackStack()
        }
    }

    private fun subscribeObserver() {
        authViewModel.auth.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    binding.signupErrorTextView.visibility = View.INVISIBLE
                    binding.signupBtnProgressBar.visibility = View.GONE
                    binding.signupBtnTextView.visibility = View.VISIBLE
                    findNavController().navigate(R.id.action_signupFragment_to_selectCategoriesFragment)
                }
                is DataState.Fail -> {
                    if(authViewModel.hasSignupError){
                        binding.signupErrorTextView.visibility = View.VISIBLE
                        binding.signupErrorTextView.text = it.message
                        binding.signupBtn.isEnabled = true
                        binding.signupBtnProgressBar.visibility = View.GONE
                        binding.signupBtnTextView.visibility = View.VISIBLE
                    }
                }
                is DataState.Loading -> {
                    binding.signupBtn.isEnabled = false
                    binding.signupBtnProgressBar.visibility = View.VISIBLE
                    binding.signupBtnTextView.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.hasSignupError = false
    }
}