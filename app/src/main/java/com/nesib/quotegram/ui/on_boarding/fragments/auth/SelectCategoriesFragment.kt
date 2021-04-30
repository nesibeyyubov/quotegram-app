package com.nesib.quotegram.ui.on_boarding.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.FragmentSelectCategoriesBinding
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.utils.Constants
import com.nesib.quotegram.utils.DataState
import java.util.*

class SelectCategoriesFragment : Fragment(R.layout.fragment_select_categories) {
    private lateinit var binding: FragmentSelectCategoriesBinding
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val args by navArgs<SelectCategoriesFragmentArgs>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectCategoriesBinding.bind(view)
        binding.selectGenreNextBtn.setOnClickListener {
            saveGenres()
        }
        binding.welcomeUserName.text = "Hi, ${args.username ?: "Guest"}"

        subscribeObservers()
    }

    private fun saveGenres() {
        val checkedIds = binding.chipGroup.checkedChipIds
        if (checkedIds.size >= Constants.MIN_GENRE_COUNT) {
            binding.warningText.visibility = View.GONE
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

    private fun subscribeObservers() {
        authViewModel.genres.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    authViewModel.saveUser()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is DataState.Loading -> {
                    binding.selectGenreNextBtn.isEnabled = false
                    binding.nextTextView.visibility = View.GONE
                    binding.nextBtnProgressBar.visibility = View.VISIBLE
                }
                is DataState.Fail -> {
                    binding.selectGenreNextBtn.isEnabled = true
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    binding.nextTextView.visibility = View.GONE
                    binding.nextBtnProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }
}