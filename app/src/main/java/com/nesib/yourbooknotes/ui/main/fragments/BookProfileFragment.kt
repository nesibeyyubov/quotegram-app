package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentBookProfileBinding
import com.nesib.yourbooknotes.databinding.FragmentHomeBinding
import com.nesib.yourbooknotes.databinding.FullPostLayoutBinding

class BookProfileFragment : Fragment(R.layout.fragment_book_profile) {
    private lateinit var binding: FragmentBookProfileBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookProfileBinding.bind(view)

        binding.addBookFromThisBookBtn.setOnClickListener {
            findNavController().navigate(R.id.action_bookProfileFragment_to_addQuoteFragment)
        }
    }
}