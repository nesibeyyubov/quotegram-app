package com.nesib.quotegram.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.FragmentQuoteBinding
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.QuoteViewModel
import com.nesib.quotegram.utils.Constants
import com.nesib.quotegram.utils.Constants.TEXT_UPDATED_QUOTE
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.toFormattedNumber
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuoteFragment : Fragment(R.layout.fragment_quote), View.OnClickListener {
    private lateinit var binding: FragmentQuoteBinding
    private val quoteViewModel: QuoteViewModel by viewModels()
    private val args: QuoteFragmentArgs by navArgs()
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })

    private var currentQuote: Quote? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentQuoteBinding.bind(view)
        subscribeObservers()
        setupClickListeners()
        setupFragmentResultListeners()

        if (args.quoteId != null) {
            if(currentQuote == null){
                quoteViewModel.getSingleQuote(args.quoteId!!)
            }else{
                bindData(currentQuote!!)
            }
        }
    }

    private fun setupClickListeners(){
        binding.usernameTextView.setOnClickListener(this)
        binding.userphotoImageView.setOnClickListener(this)
        binding.postOptionsBtn.setOnClickListener(this)
        binding.likeBtn.setOnClickListener(this)
        binding.downloadButton.setOnClickListener(this)
        binding.shareBtn.setOnClickListener(this)
        binding.tryAgainButton.setOnClickListener(this)
    }

    private fun subscribeObservers() {
        quoteViewModel.quote.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    currentQuote = it.data?.quote
                    binding.quoteContainer.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                    bindData(it.data?.quote!!)
                }
                is DataState.Loading -> {
                    binding.failContainer.visibility = View.INVISIBLE
                    binding.quoteContainer.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                }
                is DataState.Fail -> {
                    binding.failContainer.visibility = View.VISIBLE
                    binding.failMessage.text = it.message
                    binding.quoteContainer.visibility = View.INVISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun setupFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            TEXT_UPDATED_QUOTE,
            viewLifecycleOwner
        ) { s: String, bundle: Bundle ->
            val updatedQuote = (bundle[TEXT_UPDATED_QUOTE] as Quote)
            currentQuote = updatedQuote
            bindData(updatedQuote)
        }
    }

    private fun bindData(quote: Quote) {

        binding.apply {
            usernameTextView.text = quote.creator?.username
            if (quote.creator?.profileImage != "" && quote.creator?.profileImage != null) {
                userphotoImageView.load(quote.creator?.profileImage)
            } else {
                userphotoImageView.load(R.drawable.user)
            }
            quoteTextView.text = quote.quote

            likeCountTextView.text = quote.likes?.size?.toFormattedNumber()
            if (quote.liked) {
                likeBtn.setImageResource(R.drawable.ic_like_blue)
            } else {
                likeBtn.setImageResource(R.drawable.ic_like)
            }
            genreText.text = "#${quote.genre}"
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.postOptionsBtn.id -> {
                val action =
                    QuoteFragmentDirections.actionGlobalQuoteOptionsFragment(currentQuote!!)
                findNavController().navigate(action)
            }
            binding.likeBtn.id -> {
                currentQuote?.let {
                    val likes = it.likes!!.toMutableList()
                    if (!it.liked) {
                        binding.likeBtn.setImageResource(R.drawable.ic_like_blue)
                        likes.add(authViewModel.currentUserId!!)
                    } else {
                        binding.likeBtn.setImageResource(R.drawable.ic_like)
                        likes.remove(authViewModel.currentUserId)
                    }
                    it.liked = !it.liked
                    it.likes = likes.toList()
                    binding.likeCountTextView.text = it.likes!!.size.toString()
                    if (it.liked) {
                        binding.likeBtn.startAnimation(
                            AnimationUtils.loadAnimation(
                                binding.likeBtn.context,
                                R.anim.bouncing_anim
                            )
                        )
                    }
                    quoteViewModel.toggleLike(it)
                }

            }
            binding.downloadButton.id -> {
                val action = QuoteFragmentDirections.actionGlobalDownloadQuoteFragment(
                    currentQuote?.quote,
                    currentQuote?.book?.author
                )
                findNavController().navigate(action)
            }
            binding.tryAgainButton.id->{
                quoteViewModel.getSingleQuote(args.quoteId!!)
            }
            binding.shareBtn.id -> {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, currentQuote!!.quote + "\n\n#Quotegram App")
                val shareIntent = Intent.createChooser(intent, "Share Quote")
                startActivity(shareIntent)
            }
        }

    }


}