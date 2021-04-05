package com.nesib.yourbooknotes.ui.on_boarding.fragments.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentLastBinding
import com.nesib.yourbooknotes.ui.on_boarding.fragments.SplashFragmentDirections
import javax.inject.Inject

class LastFragment : Fragment(R.layout.fragment_last) {
    private lateinit var binding: FragmentLastBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLastBinding.bind(view)
        setupClickListeners()
    }
    private fun setupClickListeners(){
        binding.getStartedBtn.setOnClickListener {
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        }
        binding.skipButton.setOnClickListener {
            val action = SplashFragmentDirections.actionSplashFragmentToSelectCategoriesFragment(null,null)
            findNavController().navigate(action)
        }

    }


}