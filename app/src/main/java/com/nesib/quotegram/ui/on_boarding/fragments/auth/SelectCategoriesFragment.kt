package com.nesib.quotegram.ui.on_boarding.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.nesib.quotegram.R
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentSelectCategoriesBinding
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.utils.Constants
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.invisible
import java.util.*

class SelectCategoriesFragment :
    BaseFragment<FragmentSelectCategoriesBinding>() {
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val args by navArgs<SelectCategoriesFragmentArgs>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pbButton.setOnClickListener {
            saveGenres()
        }
        binding.welcomeUserName.text = "Hi, ${args.username ?: "Guest"}"
        binding.tvWarning.text = getString(R.string.txt_genre_warning, Constants.MIN_GENRE_COUNT)

        subscribeObservers()
    }

    private fun saveGenres() {
        val checkedIds = binding.chipGroup.checkedChipIds
        if (checkedIds.size >= Constants.MIN_GENRE_COUNT) {
            binding.tvWarning.invisible()
            var genres = ""
            var index = 0
            checkedIds.forEach { checkedId ->
                val chipText = view?.findViewById<Chip>(checkedId)?.text.toString()
                if (index < checkedIds.size - 1) {
                    genres += "${chipText.toLowerCase(Locale.ROOT)},"
                } else {
                    genres += chipText.toLowerCase(Locale.ROOT)
                }
                index++
            }
            authViewModel.saveFollowingGenres(genres, args.userId)
        }


    }

    private fun startMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun subscribeObservers() = with(binding) {
        authViewModel.genres.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    authViewModel.saveUser()
                    startMainActivity()
                }
                is DataState.Loading -> {
                    pbButton.showLoading()
                }
                is DataState.Fail -> {
                    pbButton.hideLoading()
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSelectCategoriesBinding = FragmentSelectCategoriesBinding.inflate(inflater,container,false)
}