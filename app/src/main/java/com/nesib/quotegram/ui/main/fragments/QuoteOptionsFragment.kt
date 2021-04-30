package com.nesib.quotegram.ui.main.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.MakeSureDialogLayoutBinding
import com.nesib.quotegram.databinding.QuoteOptionsLayoutBinding
import com.nesib.quotegram.databinding.ReportDialogBinding
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.QuoteViewModel
import com.nesib.quotegram.ui.viewmodels.ReportViewModel
import com.nesib.quotegram.utils.Constants.TEXT_DELETED_QUOTE
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuoteOptionsFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding: QuoteOptionsLayoutBinding
    private val args by navArgs<QuoteOptionsFragmentArgs>()
    private var makeSureDialog: AlertDialog? = null
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val reportViewModel: ReportViewModel by viewModels()

    private var deletedQuote: Quote? = null
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
    }

    private fun setupUi() {
        if (authViewModel.currentUserId != null) {
            if (args.quote.creator!!.id == authViewModel.currentUserId) {
                binding.reportQuote.visibility = View.GONE
                binding.reportUser.visibility = View.GONE
                binding.editQuote.setOnClickListener(this)
                binding.deleteQuote.setOnClickListener(this)
            } else {
                binding.deleteQuote.visibility = View.GONE
                binding.editQuote.visibility = View.GONE
                binding.reportQuote.setOnClickListener(this)
                binding.reportUser.setOnClickListener(this)
            }
        } else {
            binding.deleteQuote.visibility = View.GONE
            binding.editQuote.visibility = View.GONE
            binding.reportQuote.setOnClickListener(this)
            binding.reportUser.setOnClickListener(this)
        }
    }

    private fun subscribeObservers() {
        quoteViewModel.deleteQuote.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    parentFragmentManager.setFragmentResult(
                        TEXT_DELETED_QUOTE,
                        bundleOf(TEXT_DELETED_QUOTE to deletedQuote)
                    )
                    findNavController().popBackStack()
                    makeSureDialog!!.dismiss()
                    showToast("Deleted quote successfully")
                }
                is DataState.Fail -> {
                    findNavController().popBackStack()
                    makeSureDialog!!.dismiss()
                    showToast(it.message)
                }
                is DataState.Loading -> {

                }
            }
        }
        reportViewModel.report.observe(viewLifecycleOwner){
            when(it){
                is DataState.Success -> {
                    showToast("Reported successfully!")
                    makeSureDialog?.dismiss()
                    findNavController().popBackStack()
                }
                is DataState.Fail->{
                    makeSureDialog?.dismiss()
                    findNavController().popBackStack()
                    showToast(it.message)
                }
                is DataState.Loading->{

                }
            }
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

    private fun showMakeSureDialogForReport(quote: Quote,reportQuote:Boolean) {
        val view = layoutInflater.inflate(R.layout.report_dialog, null, false)
        val binding = ReportDialogBinding.bind(view)
        makeSureDialog = AlertDialog.Builder(requireContext()).setView(binding.root).create()
        makeSureDialog!!.show()

        binding.apply {
            notNowButton.setOnClickListener { makeSureDialog!!.dismiss() }
            reportButton.setOnClickListener {
                makeSureDialog!!.setCancelable(false)
                binding.reportProgressBar.visibility = View.VISIBLE
                binding.reportButton.visibility = View.INVISIBLE
                binding.notNowButton.isEnabled = false
                if(reportQuote){
                    reportViewModel.reportQuote(authViewModel.currentUserId ?: "",quote.id)
                }else{
                    reportViewModel.reportUser(authViewModel.currentUserId ?: "",quote.creator!!.id)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        quoteViewModel.clearLiveDataValues()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.editQuote -> {
                val action = QuoteOptionsFragmentDirections.actionGlobalAddQuoteFragment()
                action.quote = args.quote
                findNavController().popBackStack(R.id.quoteOptionsFragment, true)
                findNavController().navigate(action)
            }
            R.id.reportUser -> {
                showMakeSureDialogForReport(args.quote,false)
            }
            R.id.reportQuote -> {
                showMakeSureDialogForReport(args.quote,true)
            }
            R.id.deleteQuote->{
                showMakeSureDialog(args.quote)
            }
        }
    }


}