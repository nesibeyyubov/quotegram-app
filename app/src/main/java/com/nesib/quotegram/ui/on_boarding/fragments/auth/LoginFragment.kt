package com.nesib.quotegram.ui.on_boarding.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.nesib.quotegram.R
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentLoginBinding
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.gone
import com.nesib.quotegram.utils.invisible
import com.nesib.quotegram.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {
    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInActivityLauncher: ActivityResultLauncher<Intent>

    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })

    private var signingInWithGoogle = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerActivityResult()
        subscribeObserver()
        setupClickListeners()
    }

    private fun registerActivityResult() {
        googleSignInActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    if (task.isSuccessful) {
                        val account = task.getResult(ApiException::class.java)
                        val email = account?.email
                        val profileImage = account?.photoUrl?.toString() ?: ""

                        // do login operation here
                        if (email != null) {
                            googleSignInClient.signOut()
                            signingInWithGoogle = true
                            authViewModel.signInWithGoogle(email, profileImage)
                        } else {
                            signinWithGoogleFailed()
                        }
                    }
                } catch (e: Exception) {
                    signinWithGoogleFailed()
                }

            }
    }

    private fun signinWithGoogleFailed() = with(binding) {
        signingInWithGoogle = false
        loginErrorTextView.visible()
        loginErrorTextView.text =
            "Something went wrong, please try again"
    }

    private fun setupClickListeners() = with(binding) {
        loginBtn.setOnClickListener {
            val usernameValue = usernameEditText.text.toString()
            val passwordValue = passwordEditText.text.toString()
            if (usernameValue.isEmpty()) {
                loginErrorTextView.visible()
                loginErrorTextView.text = "Username can't be empty"
            } else if (passwordValue.isEmpty()) {
                loginErrorTextView.visible()
                loginErrorTextView.text = "Password shouldn't be empty"
            } else {
                authViewModel.login(usernameValue, passwordValue)
            }
        }
        loginToSignupBtn.setOnClickListener { navigateTo(R.id.signupFragment) }
        signInWithGoogleButton.setOnClickListener {
            val googleSignInIntent = googleSignInClient.signInIntent
            googleSignInActivityLauncher.launch(googleSignInIntent)
        }

    }

    private fun subscribeObserver() = with(binding) {
        authViewModel.auth.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    loginErrorTextView.invisible()
                    authViewModel.saveUser()
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                    requireActivity().finish()
                }
                is DataState.Fail -> {
                    signingInWithGoogle = false
                    if (authViewModel.hasLoginError) {
                        listOf(loginErrorTextView, loginBtnTextView, signInGoogleTextView).visible()
                        loginBtnProgressBar.gone()
                        signInGoogleProgressBar.invisible()
                        loginErrorTextView.text = it.message
                        loginBtn.isEnabled = true
                    }
                }
                is DataState.Loading -> {
                    loginBtn.isEnabled = false
                    if (signingInWithGoogle) {
                        signInGoogleProgressBar.visible()
                        signInGoogleTextView.gone()
                    } else {
                        loginBtnProgressBar.visible()
                        loginBtnTextView.gone()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.hasLoginError = false
    }

    override fun createBinding(view: View) = FragmentLoginBinding.bind(view)
}