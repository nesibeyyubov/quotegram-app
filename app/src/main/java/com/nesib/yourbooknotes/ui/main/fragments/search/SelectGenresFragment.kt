package com.nesib.yourbooknotes.ui.main.fragments.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.GenresAdapter
import com.nesib.yourbooknotes.databinding.FragmentSelectQuoteGenresBinding

class SelectGenresFragment:Fragment(R.layout.fragment_select_quote_genres) {
    private lateinit var binding: FragmentSelectQuoteGenresBinding
    private lateinit var genresAdapter:GenresAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectQuoteGenresBinding.bind(view)
        val genreList = resources.getStringArray(R.array.quote_genres).toMutableList()
        genreList.removeAt(0)
        genresAdapter = GenresAdapter(genreList)
        binding.genreRecyclerView.apply {
            adapter = genresAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        genresAdapter.onGenreClickListener = { genre->
            val action = SearchFragmentDirections.actionSearchFragmentToSearchQuotesFragment(genre)
            findNavController().navigate(action)
        }
    }
}