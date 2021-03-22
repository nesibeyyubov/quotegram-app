package com.nesib.yourbooknotes.ui.main.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.MakeSureDialogLayoutBinding
import com.nesib.yourbooknotes.databinding.QuoteOptionsLayoutBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.QuoteViewModel
import com.nesib.yourbooknotes.utils.DataState
import com.nesib.yourbooknotes.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuoteOptionsFragment : BottomSheetDialogFragment() {
    private lateinit var binding: QuoteOptionsLayoutBinding
    private val args by navArgs<QuoteOptionsFragmentArgs>()
    private var makeSureDialog:AlertDialog? = null
    private val authViewModel:AuthViewModel by viewModels({ requireActivity() })
    private val quoteViewModel:QuoteViewModel by viewModels({ requireActivity() })

    private var deletedQuote:Quote? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = QuoteOptionsLayoutBinding.bind(
            layoutInflater.inflate(
                R.layout.quote_options_layout,
                null,
                false
            )
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeObservers()
        setFragmentResultListeners()
    }

    private fun setupUi(){
        if (authViewModel.currentUserId != null) {
            if (args.quote.creator!!.id == authViewModel.currentUserId) {
                binding.editQuote.setOnClickListener {
                    val action = QuoteOptionsFragmentDirections.actionGlobalAddQuoteFragment()
                    action.quote = args.quote
                    findNavController().navigate(action)
                }
                binding.deleteQuote.setOnClickListener { showMakeSureDialog(args.quote) }
            } else {
                binding.deleteQuote.visibility = View.GONE
                binding.editQuote.visibility = View.GONE
            }
        }
    }

    private fun subscribeObservers(){
        quoteViewModel.deleteQuote.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    parentFragmentManager.setFragmentResult("deletedQuote", bundleOf("deletedQuote" to deletedQuote))
                    findNavController().popBackStack()
                    makeSureDialog!!.dismiss()
                    showToast("Deleted quote successfully")
                }
                is DataState.Fail -> {
                    findNavController().popBackStack()
                    makeSureDialog!!.dismiss()
                    showToast(it.message!!)
                }
                is DataState.Loading -> {

                }
            }
        }
    }

    private fun setFragmentResultListeners(){
        parentFragmentManager.setFragmentResultListener("updatedQuote",viewLifecycleOwner){_,_->
            findNavController().popBackStack()
        }
    }

    private fun showMakeSureDialog(quote: Quote) {
        val view = layoutInflater.inflate(R.layout.make_sure_dialog_layout, null, false)
        val binding = MakeSureDialogLayoutBinding.bind(view)
        makeSureDialog = AlertDialog.Builder(requireContext()).setView(binding.root).create()
        makeSureDialog!!.show()

        binding.apply {
            notDeleteButton.setOnClickListener { makeSureDialog!!.dismiss() }
            deleteButton.setOnClickListener {
                deletedQuote = quote
                makeSureDialog!!.setCancelable(false)
                binding.deleteProgressBar.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.INVISIBLE
                binding.notDeleteButton.isEnabled = false
                quoteViewModel.deleteQuote(quote)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        quoteViewModel.clearLiveDataValues()
    }


}