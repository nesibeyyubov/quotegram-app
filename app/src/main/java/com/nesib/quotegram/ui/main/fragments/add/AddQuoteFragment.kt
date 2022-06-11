package com.nesib.quotegram.ui.main.fragments.add

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
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
import com.nesib.quotegram.utils.Constants.KEY_QUOTE_BG
import com.nesib.quotegram.utils.Constants.KEY_UPDATED_QUOTE
import com.unsplash.pickerandroid.photopicker.UnsplashPhotoPicker
import com.unsplash.pickerandroid.photopicker.data.UnsplashPhoto
import com.unsplash.pickerandroid.photopicker.presentation.UnsplashPickerActivity
import com.unsplash.pickerandroid.photopicker.presentation.UnsplashPickerActivity.Companion.EXTRA_PHOTOS
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddQuoteFragment : BaseFragment<FragmentAddQuoteBinding>() {
    private val args by navArgs<AddQuoteFragmentArgs>()
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val noAuthDialog by lazy { (activity as MainActivity).dialog }
    private var updatedQuote: Quote? = null

    @Inject
    lateinit var unsplashPhotoPicker: UnsplashPhotoPicker

    private var quoteBackgroundUrl: String? = null


    companion object {
        const val REQUEST_PICK_UNSPLASH_PHOTO = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()
        initUi()
        initClickListeners()
        initUi()
    }


    private fun initClickListeners() = with(binding) {
        btnSelectBg.setOnClickListener { navigateToUnsplashPhotoPicker() }
        spinnerIcon.setOnClickListener { genreSpinner.performClick() }
        addQuoteBtn.setOnClickListener { submitQuote() }
    }

    private fun navigateToUnsplashPhotoPicker() {
        startActivityForResult(
            UnsplashPickerActivity.getStartingIntent(
                requireContext(),
                false
            ),
            REQUEST_PICK_UNSPLASH_PHOTO
        )
    }

    private fun submitQuote(update: Boolean = false) = with(binding) {
        if (authViewModel.currentUserId != null) {
            val quote = etQuote.text.toString()
            val genre = binding.genreSpinner.selectedItem.toString()
                .toLowerCase(Locale.ROOT)
            when {
                quote.length < MIN_QUOTE_LENGTH -> {
                    showSnackBar(getString(R.string.quote_name_validation, MIN_QUOTE_LENGTH))
                }
                quote.length > MAX_QUOTE_LENGTH -> {
                    showSnackBar(getString(R.string.quote_name_validation_max, MAX_QUOTE_LENGTH))
                }
                genre == resources.getString(R.string.no_genre).toLowerCase(Locale.ROOT) -> {
                    showSnackBar(getString(R.string.pls_select_genre))
                }
                else -> {
                    val newQuote =
                        mapOf(
                            KEY_QUOTE to quote,
                            KEY_GENRE to genre,
                            KEY_QUOTE_BG to (quoteBackgroundUrl ?: "")
                        )
                    if (update) {
                        updatedQuote = args.quote?.copy()
                        updatedQuote?.quote = quote
                        updatedQuote?.genre = genre
                        updatedQuote?.backgroundUrl = quoteBackgroundUrl
                        quoteViewModel.updateQuote(args.quote!!, newQuote)
                    } else {
                        quoteViewModel.postQuote(newQuote)
                    }
                }
            }
        } else {
            noAuthDialog.show()
        }
    }

    private fun initUi() = with(binding) {
        etQuote.focusWithKeyboard()

        val genres = resources.getStringArray(R.array.quote_genres).toList()
        genreSpinner.adapter = SpinnerAdapter(requireContext(), genres)
        if (args.quote != null) {
            val quote = args.quote
            addQuoteBtn.text = getString(R.string.txt_update)
            etQuote.setText(quote?.quote)
            val genresArray = resources.getStringArray(R.array.quote_genres)
            val genreIndex = genresArray.indexOf(
                quote?.genre!!.capitalize(
                    Locale.ROOT
                )
            )
            if (quote.backgroundUrl != "" && quote.backgroundUrl != null) {
                ivQuoteBg.load(quote.backgroundUrl)
                quoteOverlay.visible()
            } else {
                quoteOverlay.safeGone()
            }
            genreSpinner.adapter = SpinnerAdapter(requireContext(), genresArray.toList())
            genreSpinner.setSelection(genreIndex)

            addQuoteBtn.setOnClickListener {
                submitQuote(update = true)
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
                    binding.etQuote.unFocusWithKeyboard()
                    findNavController().popBackStack()
                    binding.addQuoteBtn.hideLoading()
                }
                is DataState.Fail -> {
                    showToast(it.message)
                    binding.addQuoteBtn.hideLoading()
                }
                is DataState.Loading -> {
                    binding.addQuoteBtn.showLoading()
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
                    binding.etQuote.unFocusWithKeyboard()
                    findNavController().popBackStack()
                    binding.addQuoteBtn.hideLoading()
                }
                is DataState.Fail -> {
                    showToast(it.message)
                    binding.addQuoteBtn.hideLoading()
                }
                is DataState.Loading -> {
                    binding.addQuoteBtn.showLoading()
                }
            }
        }
    }


    override fun onDestroyView() {
        binding.etQuote.unFocus()
        quoteViewModel.clearLiveDataValues()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_UNSPLASH_PHOTO && resultCode == RESULT_OK) {
            val photos = data?.getParcelableArrayListExtra<UnsplashPhoto>(EXTRA_PHOTOS)
            if (photos != null && photos.size > 0) {
                val selectedImage = photos[0].urls.small
                this.quoteBackgroundUrl = selectedImage
                binding.ivQuoteBg.load(selectedImage)
                binding.quoteOverlay.visible()
            }
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAddQuoteBinding = FragmentAddQuoteBinding.inflate(inflater, container, false)
}