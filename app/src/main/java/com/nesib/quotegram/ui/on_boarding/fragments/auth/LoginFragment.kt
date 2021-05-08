package com.nesib.quotegram.ui.on_boarding.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.FragmentLoginBinding
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.utils.DataState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInActivityLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: FragmentLoginBinding
    private val authViewModel: AuthViewModel by viewModels({requireActivity()})

    private var signingInWithGoogle = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        registerActivityResult()
        subscribeObserver()
        setupClickListeners()
    }

    private fun registerActivityResult() {
        googleSignInActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                Log.d("mytag", "registerActivityResult: ${task.exception?.localizedMessage}")
                if (task.isSuccessful) {
                    val account = task.result
                    val email = account?.email
                    val profileImage = account?.photoUrl?.toString() ?: ""
                    // do login operation here
                    if (email != null) {
                        googleSignInClient.signOut()
                        signingInWithGoogle = true
                        authViewModel.signInWithGoogle(email, profileImage)
                    } else {
                        signingInWithGoogle = false
                        binding.loginErrorTextView.visibility = View.VISIBLE
                        binding.loginErrorTextView.text = "Something went wrong,please try again"
                        // show something useful for user
                    }
                }else{
                    binding.loginErrorTextView.visibility = View.VISIBLE
                    binding.loginErrorTextView.text = "Something went wrong,please try again"
                }
            }
    }

    private fun setupClickListeners() {
        binding.loginBtn.setOnClickListener {
            val usernameValue = binding.usernameEditText.text.toString()
            val passwordValue = binding.passwordEditText.text.toString()
            if(usernameValue.isEmpty()){
                binding.loginErrorTextView.visibility = View.VISIBLE
                binding.loginErrorTextView.text = "Username field can't be empty"
            }
            else if(passwordValue.isEmpty()){
                binding.loginErrorTextView.visibility = View.VISIBLE
                binding.loginErrorTextView.text = "Password field shouldn't be empty"
            }
            else{
                authViewModel.login(
                    binding.usernameEditText.text.toString(),
                    binding.passwordEditText.text.toString(),
                )
            }
        }
        binding.loginToSignupBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
        binding.signInWithGoogleButton.setOnClickListener {
            val googleSignInIntent = googleSignInClient.signInIntent
            googleSignInActivityLauncher.launch(googleSignInIntent)
        }

    }

    private fun subscribeObserver() {
        authViewModel.auth.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    binding.loginErrorTextView.visibility = View.INVISIBLE
                    authViewModel.saveUser()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
                is DataState.Fail -> {
                    signingInWithGoogle = false
                    if(authViewModel.hasLoginError){
                    binding.loginErrorTextView.visibility = View.VISIBLE
                    binding.loginErrorTextView.text = it.message
                    binding.loginBtn.isEnabled = true
                    binding.loginBtnProgressBar.visibility = View.GONE
                    binding.loginBtnTextView.visibility = View.VISIBLE
                    binding.signInGoogleProgressBar.visibility = View.INVISIBLE
                    binding.signInGoogleTextView.visibility = View.VISIBLE
                    }
                }
                is DataState.Loading -> {
                    binding.loginBtn.isEnabled = false
                    if (signingInWithGoogle) {
                        binding.signInGoogleProgressBar.visibility = View.VISIBLE
                        binding.signInGoogleTextView.visibility = View.GONE
                    } else {
                        binding.loginBtnProgressBar.visibility = View.VISIBLE
                        binding.loginBtnTextView.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.hasLoginError = false
    }
}