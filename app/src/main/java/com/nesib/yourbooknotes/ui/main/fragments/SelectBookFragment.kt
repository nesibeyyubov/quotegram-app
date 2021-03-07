package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.SearchBooksAdapter
import com.nesib.yourbooknotes.databinding.FragmentSelectBookBinding
import com.nesib.yourbooknotes.ui.viewmodels.MainViewModel
import com.nesib.yourbooknotes.utils.DataState

class SelectBookFragment : Fragment(R.layout.fragment_select_book) {
    private lateinit var binding: FragmentSelectBookBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val searchAdapter by lazy { SearchBooksAdapter(true) }
    private var searchViewTextChanged = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectBookBinding.bind(view)
        subscribeObservers()
        setupRecyclerView()

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    mainViewModel.getBooks(s.toString())
                }, 300)
            }

        })
    }

    private fun setupRecyclerView() {
        searchAdapter.onBookClickListener = {book->
            val action = SelectBookFragmentDirections.actionSelectBookFragmentToAddQuoteFragment()
            action.book = book
            findNavController().navigate(action)
        }
        binding.selectBooksRecyclerView.apply {
            itemAnimator = null
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun subscribeObservers() {
        mainViewModel.books.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    searchAdapter.setData(it.data!!.books)
                    toggleProgressBar(false)
                }
                is DataState.Fail -> {
                    toggleProgressBar(false)
                }
                is DataState.Loading -> {
                    if (!searchViewTextChanged) {
                        toggleProgressBar(true)
                    }
                }
            }
        }
    }

    private fun toggleProgressBar(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        binding.selectBooksRecyclerView.visibility = if (loading) View.INVISIBLE else View.VISIBLE
    }

}