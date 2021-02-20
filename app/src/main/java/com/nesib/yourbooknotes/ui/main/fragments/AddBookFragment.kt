package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
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
        val genres = arrayOf(
            getString(R.string.genre_adventure),
            getString(R.string.genre_art),
            getString(R.string.genre_children),
            getString(R.string.genre_drama),
            getString(R.string.genre_dystopian),
            getString(R.string.genre_fantasy),
            getString(R.string.genre_health),
            getString(R.string.genre_historical_fiction),
            getString(R.string.genre_history),
            getString(R.string.genre_horror),
            getString(R.string.genre_motivational),
            getString(R.string.genre_mystery),
            getString(R.string.genre_psycholgy),
            getString(R.string.genre_science_fiction),
            getString(R.string.genre_self_help),
            getString(R.string.genre_thriller),
        )
        val adapter = GenreArrayAdapter(requireContext(),genres)
        binding.genreSpinner.adapter = adapter

    }


}