package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.BookQuotesAdapter
import com.nesib.yourbooknotes.databinding.FragmentBookProfileBinding
import com.nesib.yourbooknotes.models.Book
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.viewmodels.BookViewModel
import com.nesib.yourbooknotes.utils.Constants.API_URL
import com.nesib.yourbooknotes.utils.DataState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookProfileFragment : Fragment(R.layout.fragment_book_profile) {
    private lateinit var binding: FragmentBookProfileBinding
    private val bookViewModel: BookViewModel by viewModels()
    private val args by navArgs<BookProfileFragmentArgs>()
    private val bookQuotesAdapter by lazy { BookQuotesAdapter() }
    private var currentBook: Book? = null
    private var currentBookQuotes: MutableList<Quote>? = null
    private var paginationLoading = false
    private var currentPage = 1
    private var paginatingFinished = false

    private val quoteOptionsBottomSheet by lazy {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.post_options_layout)
        dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookProfileBinding.bind(view)
        bookViewModel.getBook(args.bookId, currentPage)
        setupClickListeners()
        setupRecyclerView()
        subscribeObservers()
        setFragmentResultListener()

    }

    private fun setFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "newQuote",
            viewLifecycleOwner
        ) { requestKey: String, newQuote: Bundle ->
            Log.d("mytag", "setFragmentResultListener: ")

            currentBookQuotes?.add(0, newQuote["newQuote"] as Quote)
            val newList = currentBookQuotes?.toList()
            newList?.let { bookQuotesAdapter.setData(it) }
        }
    }

    private fun subscribeObservers() {
        bookViewModel.book.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    binding.bookProfileContent.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    currentBook = it.data?.book!!
                    currentBookQuotes = currentBook?.quotes?.toMutableList()

                    bindData(it.data.book)
                }
                is DataState.Fail -> {
                    binding.bookProfileContent.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
                is DataState.Loading -> {
                    binding.bookProfileContent.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        bookViewModel.quotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if(currentBookQuotes?.size == it.data!!.quotes.size){
                        paginatingFinished = true
                    }
                    binding.paginationProgressBar.visibility = View.GONE
                    paginationLoading = false
                    bookQuotesAdapter.setData(it.data.quotes)
                    currentBookQuotes = it.data.quotes.toMutableList()
                }
                is DataState.Fail -> {
                    Toast.makeText(requireContext(), "Failed....", Toast.LENGTH_SHORT).show()
                    paginationLoading = false
                }
                is DataState.Loading -> {
                    paginationLoading = true
                    binding.paginationProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun setupRecyclerView() {
        bookQuotesAdapter.onQuoteOptionsClicked = {quote->
            quoteOptionsBottomSheet.show()
        }
        bookQuotesAdapter.onUserClickListener = { user ->
            user?.let {
                val action =
                    BookProfileFragmentDirections.actionBookProfileFragmentToUserProfileFragment(it.id)
                findNavController().navigate(action)
            }
        }
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.bookQuotesRecyclerView.apply {
            adapter = bookQuotesAdapter
            layoutManager = mLayoutManager
        }
        binding.bookProfileContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                val notReachedBottom = v.canScrollVertically(1)
                if (!notReachedBottom && !paginationLoading && !paginatingFinished) {
                    currentPage++
                    bookViewModel.getMoreBookQuotes(currentBook!!.id, currentPage)
                }
            }
        })

    }

    private fun bindData(book: Book) {
        binding.apply {
            bookImage.load(API_URL + book.image) {
                crossfade(400)
            }
            bookName.text = book.name
            bookAuthor.text = book.author
            bookGenre.text = book.genre
            bookQuoteCount.text = (book.totalQuoteCount ?: 0).toString()
            bookFollowerCount.text = book.followers?.size.toString()
        }
        bookQuotesAdapter.setData(currentBookQuotes!!.toList())
    }

    private fun setupClickListeners() {
        binding.addBookFromThisBookBtn.setOnClickListener {
            val action = BookProfileFragmentDirections.actionBookProfileFragmentToAddQuoteFragment()
            action.book = currentBook
            findNavController().navigate(action)
        }
    }

}