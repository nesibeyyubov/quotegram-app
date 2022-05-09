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
import com.nesib.quotegram.ui.on_boarding.StartActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.utils.*
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
        (requireActivity() as StartActivity).showToolbar()
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
        loginErrorTextView.text = getString(R.string.smthng_went_wrong)

    }

    private fun setupClickListeners() = with(binding) {
        pbButton.setOnClickListener {
            val usernameValue = usernameEditText.text.toString()
            val passwordValue = passwordEditText.text.toString()
            if (usernameValue.isEmpty()) {
                loginErrorTextView.visible()
                loginErrorTextView.text = getString(R.string.error_empty_username)
            } else if (passwordValue.isEmpty()) {
                loginErrorTextView.visible()
                loginErrorTextView.text = getString(R.string.error_empty_password)
            } else {
                authViewModel.login(usernameValue, passwordValue)
            }
        }
        loginToSignupBtn.setOnClickListener { navigateTo(R.id.signupFragment) }
        btnSignGoogle.setOnClickListener {
            val googleSignInIntent = googleSignInClient.signInIntent
            googleSignInActivityLauncher.launch(googleSignInIntent)
        }

        skipButton.setOnClickListener {
            val action =
                LoginFragmentDirections.actionLoginFragmentToSelectCategoriesFragment(null, null)
            findNavController().navigate(action)
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(requireActivity(), MainActivity::class.java))
        requireActivity().finish()
    }

    private fun subscribeObserver() = with(binding) {
        authViewModel.auth.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    loginErrorTextView.invisible()
                    authViewModel.saveUser()
                    startMainActivity()
                }
                is DataState.Fail -> {
                    signingInWithGoogle = false
                    if (authViewModel.hasLoginError) {
                        loginErrorTextView.showError(it.message)
                        pbButton.hideLoading()
                        btnSignGoogle.hideLoading()
                    }
                }
                is DataState.Loading -> {
                    if (signingInWithGoogle) {
                        btnSignGoogle.showLoading()
                    } else {
                        pbButton.showLoading()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        authViewModel.hasLoginError = false
        super.onDestroyView()
    }

    override fun createBinding(view: View) = FragmentLoginBinding.bind(view)
}