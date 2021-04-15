package com.nesib.yourbooknotes.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentHomeBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.main.MainActivity
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.QuoteViewModel
import com.nesib.yourbooknotes.utils.DataState
import com.nesib.yourbooknotes.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val authenticationDialog by lazy { (activity as MainActivity).dialog }
    private val homeAdapter by lazy { HomeAdapter(authenticationDialog) }

    private lateinit var binding: FragmentHomeBinding
    private var quotes = mutableListOf<Quote>()
    private var currentPage = 1
    private var paginationLoading = false
    private var paginatingFinished = false
    private var fetchingData = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        setupClickListeners()
        setupRecyclerView()
        subscribeObservers()

        quoteViewModel.getQuotes()
    }

    private fun setupClickListeners() {
        binding.tryAgainButton.setOnClickListener {
            quoteViewModel.getQuotes(forced = true)
        }
    }


    private fun subscribeObservers() {
        quoteViewModel.quotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if (binding.failContainer.visibility == View.VISIBLE) {
                        binding.failContainer.visibility = View.GONE
                    }
                    if (currentPage == 1) {
                        binding.shimmerLayout.visibility = View.GONE
                        binding.homeRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.shimmerLayout.visibility = View.GONE
                        binding.homeRecyclerView.visibility = View.VISIBLE

                        binding.paginationProgressBar.visibility = View.INVISIBLE
                        paginationLoading = false
                    }
                    if (quotes.size == it.data!!.quotes.size && !binding.refreshLayout.isRefreshing && fetchingData) {
                        paginatingFinished = true
                    }
                    quotes = it.data.quotes.toMutableList()

                    homeAdapter.setData(it.data.quotes)

                    if (binding.refreshLayout.isRefreshing) {
                        binding.refreshLayout.isRefreshing = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.homeRecyclerView.smoothScrollToPosition(0)
                        }, 200)
                    }
                    fetchingData = false

                }
                is DataState.Fail -> {
                    binding.apply {
                        failContainer.visibility = View.VISIBLE
                        failMessage.text = it.message
                    }
                    showToast(it.message!!)
                    binding.refreshLayout.isRefreshing = false
                    if (currentPage == 1) {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                    } else {
                        binding.paginationProgressBar.visibility = View.INVISIBLE
                        paginationLoading = false
                    }
                    fetchingData = false
                }
                is DataState.Loading -> {
                    binding.failContainer.visibility = View.GONE
                    fetchingData = true
                    if (currentPage == 1 && !binding.refreshLayout.isRefreshing) {
                        binding.shimmerLayout.visibility = View.VISIBLE
                        binding.shimmerLayout.startShimmer()
                        Log.d("mytag", "startiing shimmer...")
                    } else if (currentPage != 1 && !binding.refreshLayout.isRefreshing) {
                        binding.paginationProgressBar.visibility = View.VISIBLE
                        paginationLoading = true
                    }
                }
            }
        }

        quoteViewModel.likeQuote.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                }
                is DataState.Fail -> {
                    showToast(it.message!!)
                }
                is DataState.Loading -> {
                    Log.d("mytag", "like is loading...")
                }
            }
        }


    }

    private fun setupRecyclerView() {
        binding.refreshLayout.setOnRefreshListener {
            paginatingFinished = false
            currentPage = 1
            quoteViewModel.getQuotes(forced = true)
        }
        homeAdapter.onDownloadClickListener = { quote ->
            val action = HomeFragmentDirections.actionGlobalDownloadQuoteFragment(
                quote.quote,
                quote.book?.author
            )
            findNavController().navigate(action)
        }
        homeAdapter.onShareClickListener = { quote ->
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, quote.quote + "\n\n#Quotegram App")
            val shareIntent = Intent.createChooser(intent,"Share Quote")
            startActivity(shareIntent)

        }
        homeAdapter.currentUserId = authViewModel.currentUserId
        homeAdapter.onQuoteOptionsClickListener = { quote ->
            val action = HomeFragmentDirections.actionGlobalQuoteOptionsFragment(quote)
            findNavController().navigate(action)
        }
        homeAdapter.onLikeClickListener = { quote ->
            quoteViewModel.toggleLike(quote)
        }
        homeAdapter.OnBookClickListener = { bookId ->
            val action = HomeFragmentDirections.actionHomeFragmentToBookProfileFragment(bookId)
            findNavController().navigate(action)
        }
        homeAdapter.OnUserClickListener = { userId ->
            if (userId != authViewModel.currentUserId) {
                val action = HomeFragmentDirections.actionHomeFragmentToUserProfileFragment(userId)
                findNavController().navigate(action)
            } else {
                findNavController().navigate(R.id.action_global_myProfileFragment)
            }
        }

        val mLayoutManager = LinearLayoutManager(context)
        binding.homeRecyclerView.apply {
            adapter = homeAdapter
            layoutManager = mLayoutManager
            this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (!paginatingFinished && mLayoutManager.findLastCompletelyVisibleItemPosition() == (quotes.size - 1) && !paginationLoading) {
                            currentPage++
                            quoteViewModel.getMoreQuotes(currentPage)
                        }
                    }
                }
            })
        }

    }
}