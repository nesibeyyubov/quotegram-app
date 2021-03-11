package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentEditProfileBinding
import com.nesib.yourbooknotes.databinding.FragmentUserProfileBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.main.MainActivity
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private lateinit var binding: FragmentUserProfileBinding

    private val adapter by lazy { HomeAdapter() }
    private val userViewModel: UserViewModel by viewModels()
    private val args by navArgs<UserProfileFragmentArgs>()


    private var paginationLoading = false
    private var currentPage = 1
    private var currentUserQuotes = mutableListOf<Quote>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserProfileBinding.bind(view)

        setupClickListeners()
        setupRecyclerView()
        subscribeObservers()

        userViewModel.getUser(args.userId)
    }

    private fun subscribeObservers() {
        userViewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.profileContent.visibility = View.VISIBLE
                    val user = it.data!!.user!!
                    binding.usernameTextView.text = user.username
                    binding.bioTextView.text = if (user.bio!!.isNotEmpty()) user.bio else "No bio"
                    binding.followerCountTextView.text = user.followers!!.size.toString()
                    binding.followingCountTextView.text =
                        (user.followingUsers!!.size + user.followingBooks!!.size).toString()
                    binding.quoteCountTextView.text = user.quotes!!.size.toString()
                    adapter.setData(user.quotes)
                    currentUserQuotes = user.quotes.toMutableList()
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
                    binding.paginationProgressBar.visibility = View.INVISIBLE
                    paginationLoading = false
                    adapter.setData(it.data!!.quotes)
                }
                is DataState.Fail -> {
                    binding.paginationProgressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "Failed :${it.message}", Toast.LENGTH_SHORT)
                        .show()
                    paginationLoading = false
                }
                is DataState.Loading -> {
                    binding.paginationProgressBar.visibility = View.VISIBLE
                    paginationLoading = true

                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.followButton.setOnClickListener {

        }
    }

    private fun setupRecyclerView() {
        binding.userQuotesRecyclerView.adapter = adapter
        binding.userQuotesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.profileContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                val notReachedBottom = v.canScrollVertically(1)
                if (!notReachedBottom && !paginationLoading) {
                    currentPage++
                    userViewModel.getMoreUserQuotes(args.userId,currentPage)
                }
            }
        })
    }


}
