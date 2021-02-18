package com.nesib.yourbooknotes.ui.on_boarding.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentLastBinding
import com.nesib.yourbooknotes.databinding.FragmentSelectCategoriesBinding
import com.nesib.yourbooknotes.ui.main.MainActivity

class SelectCategoriesFragment : Fragment(R.layout.fragment_select_categories) {
    private lateinit var binding: FragmentSelectCategoriesBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectCategoriesBinding.bind(view)
        binding.selectGenreNextBtn.setOnClickListener{
            startActivity(Intent(requireContext(),MainActivity::class.java))
            requireActivity().finish()
        }
    }
}