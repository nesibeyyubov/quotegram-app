package com.nesib.quotegram.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.FullPageQuoteAdapter
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentHomeBinding
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.QuoteViewModel
import com.nesib.quotegram.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(),
    BottomNavReselectListener {
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val authenticationDialog by lazy { (activity as MainActivity).dialog }
    private val homeAdapter by lazy { FullPageQuoteAdapter(authenticationDialog) }

    private var quotesSize = 0
    private var currentPage = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBottomNavReselectListener()
        setupClickListeners()
        setupRecyclerView()
        subscribeObservers()
        quoteViewModel.getQuotes()
    }

    private fun initBottomNavReselectListener() {
        (requireActivity() as MainActivity).bottomNavItemReselectListener = this
    }

    private fun setupClickListeners() {
        binding.tryAgainButton.setOnClickListener {
            quoteViewModel.getQuotes(forced = true)
        }
    }

    private fun subscribeObservers() = with(binding) {
        quoteViewModel.quotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    failContainer.safeGone()
                    homeProgressBar.safeGone()
                    homeViewPager.safeVisible()
                    quotesSize = it.data?.quotes?.size ?: 0
                    homeAdapter.setData(it.data!!.quotes)
                    Log.d("mytag", "${it.data.quotes}")
                    if (refreshLayout.isRefreshing) {
                        refreshLayout.isRefreshing = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.homeViewPager.currentItem = 0
                        }, 200)
                    }
                }
                is DataState.Fail -> {
                    failContainer.visible()
                    failMessage.text = it.message
                    refreshLayout.isRefreshing = false
                    homeProgressBar.safeGone()
                }
                is DataState.Loading -> {
                    failContainer.safeGone()
                    if (currentPage == 1 && !binding.refreshLayout.isRefreshing) {
                        binding.homeProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.refreshLayout.setOnRefreshListener {
            currentPage = 1
            quoteViewModel.getQuotes(forced = true)
        }
        homeAdapter.onDownloadClickListener = { quote ->
            val action = HomeFragmentDirections.actionGlobalDownloadQuoteFragment(
                quote.quote,
                ""
            )
            findNavController().navigate(action)
        }
        homeAdapter.onShareClickListener = { quote ->
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, quote.quote + "\n\n#Quotegram App")
            val shareIntent = Intent.createChooser(intent, getString(R.string.share_quote))
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
                (requireActivity() as MainActivity).setSelectedItemOnBnv(R.id.myProfileFragment)
            }
        }

        binding.homeViewPager.apply {
            adapter = homeAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (position == quotesSize - 4) {
                        currentPage++
                        quoteViewModel.getMoreQuotes(currentPage)
                    }
                    super.onPageSelected(position)
                }
            })
        }

    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

    override fun itemReselected(screen: Screen?) {
        if (screen == Screen.Home) {
            binding.homeViewPager.setCurrentItem(0, true)
        }
    }
}