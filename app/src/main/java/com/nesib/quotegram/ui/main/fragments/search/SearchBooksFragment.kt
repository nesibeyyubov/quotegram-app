package com.nesib.quotegram.ui.main.fragments.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.SearchBooksAdapter
import com.nesib.quotegram.databinding.FragmentSearchBooksBinding
import com.nesib.quotegram.models.Book
import com.nesib.quotegram.ui.viewmodels.BookViewModel
import com.nesib.quotegram.ui.viewmodels.SharedViewModel
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SearchBooksFragment : Fragment(R.layout.fragment_search_books) {
    private lateinit var binding: FragmentSearchBooksBinding
    private val bookViewModel: BookViewModel by viewModels({ requireParentFragment() })
    private val sharedViewModel: SharedViewModel by viewModels({ requireActivity() })
    private val searchAdapter by lazy { SearchBooksAdapter() }

    private var searchViewTextChanged = false
    private var currentSearchText = ""
    private var currentPage = 1
    private var selectedGenre = "all"
    private var currentBooks: List<Book>? = null
    private var paginationLoading = false
    private var paginatingFinished = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBooksBinding.bind(view)
        subscribeObservers()
        setupClickListeners()
        setupRecyclerView()
        discoverBooks()
    }
    private fun setupClickListeners(){
        binding.tryAgainButton.setOnClickListener {
            bookViewModel.discoverBooks(genre = selectedGenre)
        }
    }

    private fun discoverBooks() {
        if (currentBooks!= null){
            searchAdapter.setData(currentBooks!!)
        }else{
            bookViewModel.discoverBooks()
        }
    }

    private fun setupRecyclerView() {
        searchAdapter.onBookClickListener = { book ->
            val action = SearchFragmentDirections.actionSearchFragmentToBookProfileFragment(book.id)
            findNavController().navigate(action)
        }
        binding.bookChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val newSelectedGenre =
                group.findViewById<Chip>(checkedId).text.toString().toLowerCase(Locale.ROOT)
            if (newSelectedGenre != selectedGenre) {
                currentPage = 1
                selectedGenre = newSelectedGenre
                bookViewModel.discoverBooks(selectedGenre)
            }
        }
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.searchBooksRecyclerView.apply {
            itemAnimator = null
            adapter = searchAdapter
            layoutManager = mLayoutManager
            this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (currentBooks != null &&
                            !paginatingFinished &&
                            mLayoutManager.findLastCompletelyVisibleItemPosition() == (currentBooks!!.size - 1) &&
                            !paginationLoading) {

                            paginationLoading = true
                            currentPage++
                            bookViewModel.discoverBooks(selectedGenre, currentPage, false)
                        }
                    }
                }
            })
        }

    }

    private fun subscribeObservers() {
        bookViewModel.books.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if(binding.failContainer.visibility == View.VISIBLE){
                        binding.failContainer.visibility = View.GONE
                    }
                    paginatingFinished =
                        paginationLoading && (currentBooks?.size == it.data!!.books.size)
                    searchViewTextChanged = false
                    searchAdapter.setData(it.data!!.books)
                    toggleProgressBar(false)
                    currentBooks = it.data.books
                    if (paginationLoading) {
                        paginationLoading = false
                        binding.paginationProgressBar.visibility = View.INVISIBLE
                    }
                }
                is DataState.Fail -> {
                    binding.failMessage.text = it.message
                    binding.failContainer.visibility = View.VISIBLE
                    paginationLoading = false
                    showToast(it.message!!)
                    toggleProgressBar(false)
                }
                is DataState.Loading -> {
                    if(binding.failContainer.visibility == View.VISIBLE){
                        binding.failContainer.visibility = View.GONE
                    }
                    if (!searchViewTextChanged) {
                        if (paginationLoading) {
                            binding.paginationProgressBar.visibility = View.VISIBLE
                        } else {
                            toggleProgressBar(true)
                        }
                    }

                }
            }
        }
        sharedViewModel.searchTextBook.observe(viewLifecycleOwner) { text ->
            if (currentSearchText != text) {
                searchViewTextChanged = true
                currentPage = 1
                Handler(Looper.getMainLooper())
                    .postDelayed({
                        bookViewModel.discoverBooks(selectedGenre,currentPage,searchText = text)
                    }, 300)
                currentSearchText = text
            }
        }
    }

    private fun toggleProgressBar(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        binding.searchBooksRecyclerView.visibility = if (loading) View.INVISIBLE else View.VISIBLE
    }


}