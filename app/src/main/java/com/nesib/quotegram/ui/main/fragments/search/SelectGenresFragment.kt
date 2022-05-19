package com.nesib.quotegram.ui.main.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.GenresAdapter
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentSelectQuoteGenresBinding
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SelectGenresFragment :
    BaseFragment<FragmentSelectQuoteGenresBinding>() {
    private lateinit var genresAdapter: GenresAdapter
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val sharedViewModel: SharedViewModel by viewModels({ requireActivity() })

    private lateinit var genreList: MutableList<String>
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        sharedViewModel.searchTextGenre.observe(viewLifecycleOwner) {
            val filteredList = genreList.filter { genre ->
                genre.toLowerCase(Locale.ROOT).contains(it.toLowerCase(Locale.ROOT))
            }
            genresAdapter.setData(filteredList)
        }
    }

    private fun setupRecyclerView() {
        genreList = resources.getStringArray(R.array.quote_genres).toMutableList()
        genreList.removeAt(0)
        val followingGenres = authViewModel.getFollowingGenres().split(",")
        genresAdapter = GenresAdapter(followingGenres)
        genresAdapter.setData(genreList)

        genresAdapter.onGenreClickListener = { genre ->
            sharedViewModel.toolbarText = "#${genre}"
            val action = SearchFragmentDirections.actionSearchFragmentToSearchQuotesFragment(
                genre.toLowerCase(
                    Locale.ROOT
                )
            )
            findNavController().navigate(action)
        }
        binding.genreRecyclerView.apply {
            adapter = genresAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSelectQuoteGenresBinding =
        FragmentSelectQuoteGenresBinding.inflate(inflater, container, false)
}