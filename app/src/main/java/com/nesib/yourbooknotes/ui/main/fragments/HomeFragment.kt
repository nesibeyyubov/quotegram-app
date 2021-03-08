package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentHomeBinding
import com.nesib.yourbooknotes.databinding.FullPostLayoutBinding
import com.nesib.yourbooknotes.ui.main.MainActivity
import com.nesib.yourbooknotes.ui.viewmodels.MainViewModel
import com.nesib.yourbooknotes.utils.DataState

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val adapter by lazy { HomeAdapter() }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        setupRecyclerView()
        subscribeObservers()

        mainViewModel.getQuotes()
    }

    private fun subscribeObservers() {
        mainViewModel.quotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    binding.shimmerLayout.hideShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.homeRecyclerView.visibility = View.VISIBLE
                    adapter.setData(it.data!!.quotes)
                }
                is DataState.Fail -> {
                    binding.shimmerLayout.hideShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                }
                is DataState.Loading -> {
                    binding.shimmerLayout.startShimmer()
                }
            }
        }
    }

    private fun setupRecyclerView() {

        adapter.OnBookClickListener = { bookId ->
            val action = HomeFragmentDirections.actionHomeFragmentToBookProfileFragment(bookId)
            findNavController().navigate(action)
        }
        adapter.OnUserClickListener = { userId ->
            val action = HomeFragmentDirections.actionHomeFragmentToUserProfileFragment(userId)
            findNavController().navigate(action)
        }
        binding.homeRecyclerView.adapter = adapter
        binding.homeRecyclerView.layoutManager = LinearLayoutManager(context)

    }
}