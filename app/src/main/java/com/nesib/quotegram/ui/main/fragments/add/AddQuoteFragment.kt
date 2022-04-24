package com.nesib.quotegram.ui.main.fragments.add

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.SpinnerAdapter
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentAddQuoteBinding
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.QuoteViewModel
import com.nesib.quotegram.utils.*
import com.nesib.quotegram.utils.Constants.KEY_GENRE
import com.nesib.quotegram.utils.Constants.MAX_QUOTE_LENGTH
import com.nesib.quotegram.utils.Constants.MIN_QUOTE_LENGTH
import com.nesib.quotegram.utils.Constants.KEY_NEW_QUOTE
import com.nesib.quotegram.utils.Constants.KEY_QUOTE
import com.nesib.quotegram.utils.Constants.KEY_UPDATED_QUOTE
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AddQuoteFragment : BaseFragment<FragmentAddQuoteBinding>(R.layout.fragment_add_quote) {
    private val args by navArgs<AddQuoteFragmentArgs>()
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val noAuthDialog by lazy { (activity as MainActivity).dialog }
    private var updatedQuote: Quote? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeObservers()
        setupUi()
    }

    private fun setupUi() = with(binding) {
        quoteEditText.focusWithKeyboard()
        spinnerIcon.setOnClickListener {
            genreSpinner.performClick()
        }
        addQuoteBtn.setOnClickListener {
            if (authViewModel.currentUserId != null) {
                val quote = quoteEditText.text.toString()
                val selectedGenre = binding.genreSpinner.selectedItem.toString()
                    .toLowerCase(Locale.ROOT)

                if (quote.length < MIN_QUOTE_LENGTH) {
                    binding.addQuoteErrorTextView.visibility = View.VISIBLE
                    binding.addQuoteErrorTextView.text = getString(
                        R.string.quote_name_validation,
                        MIN_QUOTE_LENGTH
                    )
                } else if (quote.length > MAX_QUOTE_LENGTH) {
                    binding.addQuoteErrorTextView.visibility = View.VISIBLE
                    binding.addQuoteErrorTextView.text = getString(
                        R.string.quote_name_validation_max,
                        MAX_QUOTE_LENGTH
                    )
                } else if (selectedGenre == resources.getString(R.string.no_genre)
                        .toLowerCase(
                            Locale.ROOT
                        )
                ) {
                    binding.addQuoteErrorTextView.visibility = View.VISIBLE
                    binding.addQuoteErrorTextView.text = getString(R.string.pls_select_genre)
                } else {
                    binding.addQuoteErrorTextView.visibility = View.INVISIBLE
                    val newQuote =
                        mapOf(KEY_QUOTE to quote, KEY_GENRE to selectedGenre)
                    quoteViewModel.postQuote(newQuote)
                }
            } else {
                noAuthDialog.show()
            }
        }
        val genres = resources.getStringArray(R.array.quote_genres).toList()
        genreSpinner.adapter = SpinnerAdapter(requireContext(), genres)
        if (args.quote != null) {
            val quote = args.quote
            addBtnTextView.text = getString(R.string.txt_update)
            quoteEditText.setText(quote?.quote)
            val genresArray = resources.getStringArray(R.array.quote_genres)
            val genreIndex = genresArray.indexOf(
                quote?.genre!!.capitalize(
                    Locale.ROOT
                )
            )
            genreSpinner.adapter = SpinnerAdapter(requireContext(), genresArray.toList())
            genreSpinner.setSelection(genreIndex)

            addQuoteBtn.setOnClickListener {
                val quoteValue = quoteEditText.text.toString()
                val selectedGenreValue = binding.genreSpinner.selectedItem.toString()
                    .toLowerCase(Locale.ROOT)
                val newQuote =
                    mapOf(
                        KEY_QUOTE to quoteValue,
                        KEY_GENRE to selectedGenreValue
                    )
                updatedQuote = args.quote!!.copy()
                updatedQuote!!.quote = quoteValue
                updatedQuote!!.genre = selectedGenreValue

                quoteViewModel.updateQuote(args.quote!!, newQuote)
            }
        }
    }


    private fun subscribeObservers() {
        quoteViewModel.quote.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    showToast(getString(R.string.action_quote_addad))
                    parentFragmentManager.setFragmentResult(
                        KEY_NEW_QUOTE,
                        bundleOf(KEY_NEW_QUOTE to it.data!!.quote)
                    )
                    binding.quoteEditText.unFocusWithKeyboard()
                    findNavController().popBackStack()
                    toggleProgressBar(false)
                }
                is DataState.Fail -> {
                    showToast(it.message)
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
                        KEY_UPDATED_QUOTE,
                        bundleOf(KEY_UPDATED_QUOTE to updatedQuote)
                    )
                    binding.quoteEditText.unFocusWithKeyboard()
                    findNavController().popBackStack()
                    toggleProgressBar(false)
                }
                is DataState.Fail -> {
                    showToast(it.message)
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
        binding.quoteEditText.unFocus()
        quoteViewModel.clearLiveDataValues()
        super.onDestroyView()
    }

    override fun createBinding(view: View) = FragmentAddQuoteBinding.bind(view)
}