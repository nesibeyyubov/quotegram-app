package com.nesib.yourbooknotes.ui.on_boarding.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentLastBinding
import com.nesib.yourbooknotes.databinding.FragmentSelectCategoriesBinding
import com.nesib.yourbooknotes.ui.main.MainActivity
import com.nesib.yourbooknotes.ui.on_boarding.StartActivity
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.utils.DataState
import java.util.*

class SelectCategoriesFragment : Fragment(R.layout.fragment_select_categories) {
    private lateinit var binding: FragmentSelectCategoriesBinding
    private lateinit var authViewModel: AuthViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectCategoriesBinding.bind(view)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        binding.selectGenreNextBtn.setOnClickListener {
            saveGenres()
        }
        subscribeObservers()

    }

    private fun saveGenres() {
        val checkedIds = binding.chipGroup.checkedChipIds
        if (checkedIds.size >= 3) {
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
            authViewModel.saveFollowingGenres(genres)
        }


    }

    private fun subscribeObservers() {
        authViewModel.genres.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    val user = it.data?.user
                    authViewModel.saveUser()
                    if(user != null){
                        authViewModel.saveExtraUserDetail(
                            user.username!!,
                            user.email!!,
                            user.profileImage ?: ""
                        )
                    }
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