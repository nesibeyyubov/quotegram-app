package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentAddQuoteBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.viewmodels.MainViewModel
import com.nesib.yourbooknotes.utils.Constants.API_URL
import com.nesib.yourbooknotes.utils.DataState

class AddQuoteFragment : BottomSheetDialogFragment() {
    private val args by navArgs<AddQuoteFragmentArgs>()
    private lateinit var binding: FragmentAddQuoteBinding
    private var selectedGenre =""
    private val mainViewModel:MainViewModel by viewModels()
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
        setupClickListeners()

    }

    private fun setupClickListeners(){
        binding.genreSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(position != 0){
                    selectedGenre = binding.genreSpinner.selectedItem.toString()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    private fun setupUi(){
        if (args.book != null) {
            binding.apply {
                val book = args.book
                bookAuthorTextView.text = book!!.author
                bookNameTextView.text = book.name
                bookImage.load(API_URL + book.image)

                addQuoteBtn.setOnClickListener {
                    val quote = quoteEditText.text.toString()
                    val newQuote = mapOf("book" to book.id,"quote" to quote,"genre" to selectedGenre)
                    mainViewModel.postQuote(newQuote)
                }
            }
        }
    }


    private fun subscribeObservers(){
        mainViewModel.quote.observe(viewLifecycleOwner){
            when(it){
                is DataState.Success->{
                    Log.d("mytag", "Success:${it.message} ")
                    findNavController().popBackStack()
                    toggleProgressBar(false)
                }
                is DataState.Fail -> {
                    Log.d("mytag", "Fail:${it.message} ")
                    toggleProgressBar(false)
                }
                is DataState.Loading->{
                    toggleProgressBar(true)
                }
            }
        }
    }

    private fun toggleProgressBar(loading:Boolean){
        binding.addBtnTextView.visibility = if(loading) View.GONE else View.VISIBLE
        binding.progressBar.visibility = if(loading) View.VISIBLE else View.GONE
    }
}