package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentAddQuoteBinding
import com.nesib.yourbooknotes.utils.Constants.API_URL

class AddQuoteFragment : BottomSheetDialogFragment() {
    private val args by navArgs<AddQuoteFragmentArgs>()
    private lateinit var binding: FragmentAddQuoteBinding
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
        if (args.book != null) {
            binding.apply {
//                bookAuthorTextView.text = args.book?.author
//                bookNameTextView.text = args.book?.name
//                bookImage.load(API_URL + args.book?.image)
            }
        }
    }
}