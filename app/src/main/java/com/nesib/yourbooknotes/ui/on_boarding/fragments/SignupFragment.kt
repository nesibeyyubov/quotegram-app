package com.nesib.yourbooknotes.ui.on_boarding.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentLoginBinding
import com.nesib.yourbooknotes.databinding.FragmentSignupBinding
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState

class SignupFragment : Fragment(R.layout.fragment_signup) {
    private lateinit var binding: FragmentSignupBinding
    private val authViewModel:AuthViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignupBinding.bind(view)

        subscribeObserver()

        binding.signupBtn.setOnClickListener{
            val username = binding.usernameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            authViewModel.signup(email,password,username,username)
        }
        binding.signupToLoginBtn.setOnClickListener {
            // Check if it is coming from login then pop back
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }

    private fun subscribeObserver(){
        authViewModel.auth.observe(viewLifecycleOwner){
            when(it){
                is DataState.Success -> {
                    binding.signupBtnProgressBar.visibility = View.GONE
                    binding.signupBtnTextView.visibility = View.VISIBLE
                    findNavController().navigate(R.id.action_signupFragment_to_selectCategoriesFragment)
                }
                is DataState.Fail -> {
                    Log.d("mytag", "fail reason: ${it.message}")
                    binding.signupBtnProgressBar.visibility = View.GONE
                    binding.signupBtnTextView.visibility = View.VISIBLE
                }
                is DataState.Loading -> {
                    binding.signupBtnProgressBar.visibility = View.VISIBLE
                    binding.signupBtnTextView.visibility = View.GONE
                }
            }
        }
    }
}