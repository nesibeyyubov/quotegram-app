package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentUserProfileBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.User
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private lateinit var binding: FragmentUserProfileBinding

    private val homeAdapter by lazy { HomeAdapter() }
    private val userViewModel: UserViewModel by viewModels()
    private val args by navArgs<UserProfileFragmentArgs>()

    private var paginatingFinished = false
    private var paginationLoading = false
    private var currentPage = 1
    private var currentUserQuotes = mutableListOf<Quote>()

    private val quoteOptionsBottomSheet by lazy {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(R.layout.post_options_layout)
        dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserProfileBinding.bind(view)

        setupClickListeners()
        setupRecyclerView()
        subscribeObservers()

        userViewModel.getUser(args.userId)
    }

    private fun bindData(user: User) {
        binding.usernameTextView.text = user.username
        binding.bioTextView.text = if (user.bio!!.isNotEmpty()) user.bio else "No bio"
        binding.followerCountTextView.text = user.followers!!.size.toString()
        binding.followingCountTextView.text =
            (user.followingUsers!!.size + user.followingBooks!!.size).toString()
        binding.quoteCountTextView.text = (user.totalQuoteCount ?: 0).toString()
        homeAdapter.setData(user.quotes!!)
    }

    private fun subscribeObservers() {
        userViewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.profileContent.visibility = View.VISIBLE
                    val user = it.data!!.user!!
                    bindData(user)
                    currentUserQuotes = user.quotes!!.toMutableList()
                }
                is DataState.Fail -> {
                    binding.progressBar.visibility = View.GONE
                    binding.profileContent.visibility = View.VISIBLE
                }
                is DataState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.profileContent.visibility = View.GONE
                }
            }
        }
        userViewModel.userQuotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if (currentUserQuotes.size == it.data!!.quotes.size) {
                        paginatingFinished = true
                    }
                    binding.paginationProgressBar.visibility = View.INVISIBLE
                    paginationLoading = false
                    homeAdapter.setData(it.data.quotes)
                    currentUserQuotes = it.data.quotes.toMutableList()

                }
                is DataState.Fail -> {
                    binding.paginationProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "Failed :${it.message}", Toast.LENGTH_SHORT)
                        .show()
                    paginationLoading = false
                }
                is DataState.Loading -> {
                    if (paginationLoading) {
                        binding.paginationProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.followButton.setOnClickListener {

        }
    }

    private fun setupRecyclerView() {
        homeAdapter.onQuoteOptionsClickListener = {quote->
            quoteOptionsBottomSheet.show()
        }
        binding.userQuotesRecyclerView.adapter = homeAdapter
        binding.userQuotesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.profileContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                val notReachedBottom = v.canScrollVertically(1)

                if (!notReachedBottom && !paginationLoading && currentUserQuotes.size >= 2 && !paginatingFinished) {
                    currentPage++
                    paginationLoading = true
                    userViewModel.getMoreUserQuotes(args.userId, currentPage)
                }
            }
        })
    }


}
