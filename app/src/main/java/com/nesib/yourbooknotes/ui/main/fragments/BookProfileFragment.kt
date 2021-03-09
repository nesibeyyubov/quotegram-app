package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.BookQuotesAdapter
import com.nesib.yourbooknotes.databinding.FragmentBookProfileBinding
import com.nesib.yourbooknotes.models.Book
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.viewmodels.MainViewModel
import com.nesib.yourbooknotes.utils.DataState

class BookProfileFragment : Fragment(R.layout.fragment_book_profile) {
    private lateinit var binding: FragmentBookProfileBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val args by navArgs<BookProfileFragmentArgs>()
    private val bookQuotesAdapter by lazy { BookQuotesAdapter() }
    private var currentBook: Book? = null
    private var currentBookQuotes: MutableList<Quote>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookProfileBinding.bind(view)
        mainViewModel.getBook(args.bookId)
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

            currentBookQuotes?.add(0, newQuote["newQuote"] as Quote)
            val newList = currentBookQuotes?.toList()
            bookQuotesAdapter.setData(newList!!)
        }
    }

    private fun subscribeObservers() {
        mainViewModel.book.observe(viewLifecycleOwner) {
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
    }


    private fun setupRecyclerView() {
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.bookQuotesRecyclerView.apply {
            adapter = bookQuotesAdapter
            isNestedScrollingEnabled = false
            layoutManager = mLayoutManager
        }








    }

    private fun bindData(book: Book) {
        binding.apply {
            bookImage.load("http://10.0.2.2:4000/" + book.image) {
                crossfade(400)
            }
            bookName.text = book.name
            bookAuthor.text = book.author
            bookGenre.text = book.genre
            bookQuoteCount.text = book.quotes?.size.toString()
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