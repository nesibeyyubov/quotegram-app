package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentAddQuoteBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.viewmodels.QuoteViewModel
import com.nesib.yourbooknotes.utils.Constants.API_URL
import com.nesib.yourbooknotes.utils.DataState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.selects.select
import java.util.*

@AndroidEntryPoint
class AddQuoteFragment : BottomSheetDialogFragment() {
    private val args by navArgs<AddQuoteFragmentArgs>()
    private lateinit var binding: FragmentAddQuoteBinding
    private val quoteViewModel: QuoteViewModel by viewModels({requireActivity()})

    private var updatedQuote: Quote? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_add_quote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddQuoteBinding.bind(view)
        subscribeObservers()
        setupUi()

    }


    private fun setupUi() {
        if (args.book != null) {
            binding.apply {
                val book = args.book
                bookAuthorTextView.text = book!!.author
                bookNameTextView.text = book.name
                bookImage.load(API_URL + book.image)

                addQuoteBtn.setOnClickListener {
                    val quote = quoteEditText.text.toString()
                    val selectedGenre = binding.genreSpinner.selectedItem.toString()
                        .toLowerCase(Locale.ROOT)
                    val newQuote =
                        mapOf("book" to book.id, "quote" to quote, "genre" to selectedGenre)
                    quoteViewModel.postQuote(newQuote)
                }
            }
        }
        if (args.quote != null) {
            binding.apply {
                val quote = args.quote
                bookAuthorTextView.text = quote!!.book!!.author
                bookNameTextView.text = quote.book!!.name
                bookImage.load(API_URL + quote.book!!.image)
                addBtnTextView.text = "Update"
                quoteEditText.setText(quote.quote)
                genreSpinner.setSelection(
                    resources.getStringArray(R.array.quote_genres).indexOf(
                        quote.genre!!.capitalize(
                            Locale.ROOT
                        )
                    )
                )

                addQuoteBtn.setOnClickListener {
                    val quoteValue = quoteEditText.text.toString()
                    val selectedGenreValue = binding.genreSpinner.selectedItem.toString()
                        .toLowerCase(Locale.ROOT)
                    val newQuote =
                        mapOf(
                            "quote" to quoteValue,
                            "genre" to selectedGenreValue
                        )
                    updatedQuote = args.quote!!.copy()
                    updatedQuote!!.quote = quoteValue
                    updatedQuote!!.genre = selectedGenreValue

                    quoteViewModel.updateQuote(args.quote!!,newQuote)
                }
            }
        }
    }


    private fun subscribeObservers() {
        quoteViewModel.quote.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    parentFragmentManager.setFragmentResult(
                        "newQuote",
                        bundleOf("newQuote" to it.data!!.quote)
                    )
                    findNavController().popBackStack()
                    toggleProgressBar(false)
                }
                is DataState.Fail -> {
                    toggleProgressBar(false)
                }
                is DataState.Loading -> {
                    toggleProgressBar(true)
                }
            }
        }
        quoteViewModel.updateQuote.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    parentFragmentManager.setFragmentResult(
                        "updatedQuote",
                        bundleOf("updatedQuote" to updatedQuote)
                    )
                    findNavController().popBackStack()
                    toggleProgressBar(false)
                }
                is DataState.Fail -> {
                    toggleProgressBar(false)
                }
                is DataState.Loading -> {
                    toggleProgressBar(true)
                }
            }
        }
    }

    private fun toggleProgressBar(loading: Boolean) {
        binding.addBtnTextView.visibility = if (loading) View.GONE else View.VISIBLE
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}