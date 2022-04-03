package com.nesib.quotegram.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.FullPageQuoteAdapter
import com.nesib.quotegram.adapters.HomeAdapter
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentHomeBinding
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.QuoteViewModel
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val authenticationDialog by lazy { (activity as MainActivity).dialog }
    private val homeAdapter by lazy { FullPageQuoteAdapter(authenticationDialog) }

    private var quotes = mutableListOf<Quote>()
    private var currentPage = 1
    private var paginationLoading = false
    private var paginatingFinished = false
    private var fetchingData = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                        binding.homeProgressBar.visibility = View.GONE
//                        binding.homeRecyclerView.visibility = View.VISIBLE
                        binding.homeViewPager.visibility = View.VISIBLE
                    } else {
                        binding.homeProgressBar.visibility = View.GONE
//                        binding.homeRecyclerView.visibility = View.VISIBLE
                        binding.homeViewPager.visibility = View.VISIBLE

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
                            binding.homeViewPager.currentItem = 0
                        }, 200)
                    }
                    fetchingData = false

                }
                is DataState.Fail -> {
                    binding.apply {
                        failContainer.visibility = View.VISIBLE
                        failMessage.text = it.message
                    }
                    binding.refreshLayout.isRefreshing = false
                    if (currentPage == 1) {
                        binding.homeProgressBar.visibility = View.GONE
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
                        binding.homeProgressBar.visibility = View.VISIBLE
                    } else if (currentPage != 1 && !binding.refreshLayout.isRefreshing) {
                        binding.paginationProgressBar.visibility = View.VISIBLE
                        paginationLoading = true
                    }
                }
            }
        }

        quoteViewModel.likeQuote.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Fail -> {
                    showToast(it.message)
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
                quote.book?.name
            )
            findNavController().navigate(action)
        }
        homeAdapter.onShareClickListener = { quote ->
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, quote.quote + "\n\n#Quotegram App")
            val shareIntent = Intent.createChooser(intent, "Share Quote")
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

        homeAdapter.OnUserClickListener = { userId ->
            if (userId != authViewModel.currentUserId) {
                val action = HomeFragmentDirections.actionHomeFragmentToUserProfileFragment(userId)
                findNavController().navigate(action)
            } else {
                findNavController().navigate(R.id.action_global_myProfileFragment)
            }
        }

        val mLayoutManager = LinearLayoutManager(context)

        binding.homeViewPager.apply {
            adapter = homeAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position + 1 == quotes.size && !paginatingFinished) {
                        currentPage++
                        quoteViewModel.getMoreQuotes(currentPage)
                    }
                    super.onPageSelected(position)
                }
            })
        }
//        binding.homeViewPager.apply {
//            adapter = homeAdapter
//            layoutManager = mLayoutManager
//            this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    if (dy > 0) {
//                        if (!paginatingFinished && mLayoutManager.findLastCompletelyVisibleItemPosition() == (quotes.size - 1) && !paginationLoading) {
//                            currentPage++
//                            quoteViewModel.getMoreQuotes(currentPage)
//                        }
//                    }
//                }
//            })
//        }

    }

    override fun createBinding(view: View) = FragmentHomeBinding.bind(view)
}