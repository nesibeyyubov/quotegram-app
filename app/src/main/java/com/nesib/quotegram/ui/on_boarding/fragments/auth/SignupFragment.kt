package com.nesib.quotegram.ui.on_boarding.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.nesib.quotegram.R
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentSignupBinding
import com.nesib.quotegram.models.UserAuth
import com.nesib.quotegram.ui.on_boarding.StartActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.utils.*
import com.nesib.quotegram.utils.Constants.MIN_PASSWORD_LENGTH
import com.nesib.quotegram.utils.Constants.MIN_USERNAME_LENGTH
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class SignupFragment : BaseFragment<FragmentSignupBinding>(R.layout.fragment_signup) {
    @Inject
    lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInActivityLauncher: ActivityResultLauncher<Intent>
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private var signingWithGoogle = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerActivityResult()
        subscribeObserver()
        setupClickListeners()
        (requireActivity() as StartActivity).showToolbar()
    }

    private fun setupClickListeners() = with(binding) {
        pbButton.setOnClickListener {
            signup()
        }
        signupToLoginBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        signupBtnGoogle.setOnClickListener {
            val googleSignInIntent = googleSignInClient.signInIntent
            googleSignInActivityLauncher.launch(googleSignInIntent)
        }
    }

    private fun signup() = with(binding) {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()
        if (!username.isValidUsername()) {
            signupErrorTextView.showError(getString(R.string.error_username, MIN_USERNAME_LENGTH))
        } else if (!password.isValidPassword()) {
            signupErrorTextView.showError(getString(R.string.error_password, MIN_PASSWORD_LENGTH))
        } else {
            authViewModel.signup(username, password)
        }
    }

    private fun signupWithGoogle(account: GoogleSignInAccount?) {
        val username = account?.email?.split("@")?.get(0) + Random.nextInt(0, 9999)
        val fullName = account?.displayName
        val email = account?.email
        val profileImage = account?.photoUrl?.toString()
        // do signup operation here
        if (email != null && fullName != null && username != null) {
            signingWithGoogle = true
            googleSignInClient.signOut()
            authViewModel.signupWithGoogle(
                email,
                fullName,
                username,
                profileImage ?: ""
            )
        } else {
            binding.signupErrorTextView.showError(getString(R.string.smthng_went_wrong))
        }
    }

    private fun registerActivityResult() = with(binding) {
        googleSignInActivityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    if (task.isSuccessful) {
                        val account = task.getResult(ApiException::class.java)
                        signupWithGoogle(account)
                    }
                } catch (e: Exception) {
                    signupErrorTextView.showError(getString(R.string.smthng_went_wrong))
                }
            }
    }

    private fun navigateToCategories(data: UserAuth) {
        val action =
            SignupFragmentDirections.actionSignupFragmentToSelectCategoriesFragment(
                data.userId,
                data.username
            )
        findNavController().navigate(action)
    }

    private fun subscribeObserver() = with(binding) {
        authViewModel.auth.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    signupErrorTextView.invisible()
                    authViewModel.saveUser()
                    navigateToCategories(it.data!!)
                }
                is DataState.Fail -> {
                    signingWithGoogle = false
                    if (authViewModel.hasSignupError) {
                        signupErrorTextView.showError(it.message)
                        pbButton.hideLoading()
                        signupBtnGoogle.hideLoading()
                    }
                }
                is DataState.Loading -> {
                    if (signingWithGoogle) {
                        signupBtnGoogle.showLoading()
                    } else {
                        pbButton.showLoading()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.hasSignupError = false
    }

    override fun createBinding(view: View) = FragmentSignupBinding.bind(view)
}