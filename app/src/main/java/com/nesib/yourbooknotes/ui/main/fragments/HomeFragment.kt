package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentHomeBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.MainViewModel
import com.nesib.yourbooknotes.utils.DataState

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var authViewModel: AuthViewModel
    private val homeAdapter by lazy { HomeAdapter() }
    private var quotes = mutableListOf<Quote>()
    private var currentPage = 1
    private var paginationLoading = false
    private var paginatingFinished = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        setupRecyclerView()
        subscribeObservers()

        mainViewModel.getQuotes()
    }

    private fun subscribeObservers() {
        mainViewModel.quotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if (currentPage == 1) {
                        binding.shimmerLayout.visibility = View.GONE
                        binding.homeRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.shimmerLayout.visibility = View.GONE
                        binding.homeRecyclerView.visibility = View.VISIBLE

                        binding.paginationProgressBar.visibility = View.INVISIBLE
                        paginationLoading = false
                    }
                    if (quotes.size == it.data!!.quotes.size) {
                        paginatingFinished = true
                    }
                    quotes = it.data.quotes.toMutableList()

                    homeAdapter.setData(it.data.quotes)

                }
                is DataState.Fail -> {
                    if (currentPage == 1) {
                        binding.shimmerLayout.hideShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                    } else {
                        binding.paginationProgressBar.visibility = View.INVISIBLE
                        paginationLoading = false
                    }
                }
                is DataState.Loading -> {
                    if (currentPage == 1) {
                        binding.shimmerLayout.startShimmer()
                    } else {
                        binding.paginationProgressBar.visibility = View.VISIBLE
                        paginationLoading = true
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {

        homeAdapter.OnBookClickListener = { bookId ->
            val action = HomeFragmentDirections.actionHomeFragmentToBookProfileFragment(bookId)
            findNavController().navigate(action)
        }
        homeAdapter.OnUserClickListener = { userId ->
            val action = HomeFragmentDirections.actionHomeFragmentToUserProfileFragment(userId)
            findNavController().navigate(action)
        }
        val mLayoutManager = LinearLayoutManager(context)
        binding.homeRecyclerView.apply {
            adapter = homeAdapter
            layoutManager = mLayoutManager
            this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (!paginatingFinished && mLayoutManager.findLastCompletelyVisibleItemPosition() == (quotes.size - 1) && !paginationLoading) {
                            currentPage++
                            mainViewModel.getMoreQuotes(currentPage)
                        }
                    }
                }
            })
        }

    }
}