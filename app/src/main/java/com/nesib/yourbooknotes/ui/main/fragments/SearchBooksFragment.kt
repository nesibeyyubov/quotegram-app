package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.SearchBooksAdapter
import com.nesib.yourbooknotes.databinding.FragmentSearchBinding
import com.nesib.yourbooknotes.databinding.FragmentSearchBooksBinding
import com.nesib.yourbooknotes.ui.main.MainActivity
import com.nesib.yourbooknotes.ui.viewmodels.MainViewModel
import com.nesib.yourbooknotes.utils.DataState
import com.nesib.yourbooknotes.utils.IBooksNotifer
import com.nesib.yourbooknotes.utils.Utils

class SearchBooksFragment : Fragment(R.layout.fragment_search_books),IBooksNotifer {
    private lateinit var binding: FragmentSearchBooksBinding
    private val mainViewModel:MainViewModel by viewModels()
    private val adapter by lazy { SearchBooksAdapter() }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.booksNotifier = this
        binding = FragmentSearchBooksBinding.bind(view)
        subscribeObservers()
        setupRecyclerView()

        mainViewModel.getBooks()
    }

    private fun setupRecyclerView(){
        binding.searchBooksRecyclerView.adapter = adapter
        binding.searchBooksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeObservers(){
        mainViewModel.books.observe(viewLifecycleOwner){
            when(it){
                is DataState.Success->{
                    toggleProgressBar(false)
                    adapter.setData(it.data!!.books)
                }
                is DataState.Fail->{
                    toggleProgressBar(false)
                }
                is DataState.Loading -> {
                    toggleProgressBar(true)
                }
            }
        }
    }

    private fun toggleProgressBar(loading:Boolean){
        binding.progressBar.visibility = if(loading) View.VISIBLE else View.GONE
        binding.searchBooksRecyclerView.visibility = if(loading) View.GONE else View.VISIBLE
    }

    override fun notify(text: String) {
        Log.d("mytag", "book notify: $text")
    }


}