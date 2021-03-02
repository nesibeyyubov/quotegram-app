package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.GenreArrayAdapter
import com.nesib.yourbooknotes.databinding.FragmentAddBookBinding

class AddBookFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentAddBookBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBookBinding.inflate(inflater)
        return layoutInflater.inflate(R.layout.fragment_add_book, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val genres = resources.getStringArray(R.array.book_genres)
        val adapter = GenreArrayAdapter(requireActivity(), genres)
        binding.genreSpinner.adapter = adapter


    }


}