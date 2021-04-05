package com.nesib.yourbooknotes.ui.main.fragments.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentMyProfileBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.User
import com.nesib.yourbooknotes.ui.main.MainActivity
import com.nesib.yourbooknotes.ui.on_boarding.StartActivity
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.QuoteViewModel
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyProfileFragment : Fragment(R.layout.fragment_my_profile) {
    private lateinit var binding: FragmentMyProfileBinding

    private val homeAdapter by lazy { HomeAdapter((activity as MainActivity).dialog) }
    private val userViewModel: UserViewModel by viewModels()
    private val quoteViewModel: QuoteViewModel by viewModels({requireActivity()})
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })

    private var paginationLoading = false
    private var currentPage = 1
    private var paginationFinished = false
    private var currentUserQuotes: MutableList<Quote>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyProfileBinding.bind(view)

        setupClickListeners()
        setFragmentResultListener()
        setupRecyclerView()
        subscribeObservers()
        getUser()
    }

    private fun getUser() {
        if (authViewModel.isAuthenticated) {
            userViewModel.getUser()
        } else {
            binding.notSignedinContainer.visibility = View.VISIBLE
        }
    }

    private fun subscribeObservers() {
        userViewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    toggleProgressBar(false)
                    val user = it.data!!.user!!
                    currentUserQuotes = user.quotes!!.toMutableList()
                    bindData(user)
                }
                is DataState.Fail -> {
                    toggleProgressBar(false)
                }
                is DataState.Loading -> {
                    toggleProgressBar(true)
                }
            }
        }
        userViewModel.userQuotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if (currentUserQuotes?.size == it.data!!.quotes.size) {
                        paginationFinished = true
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
                    binding.paginationProgressBar.visibility = View.VISIBLE
                    paginationLoading = true

                }
            }
        }
    }

    private fun toggleProgressBar(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.profileContent.visibility = if (loading) View.GONE else View.VISIBLE
        binding.notSignedinContainer.visibility = View.GONE
    }

    private fun bindData(user: User) {
        binding.usernameTextView.text = user.username
        binding.bioTextView.text = if (user.bio!!.isNotEmpty()) user.bio else "No bio"
        binding.followerCountTextView.text = user.followers!!.size.toString()
        binding.followingCountTextView.text =
            (user.followingUsers!!.size + user.followingBooks!!.size).toString()
        binding.quoteCountTextView.text = (user.totalQuoteCount ?: 0).toString()
        binding.quoteCountTextView.text = (user.totalQuoteCount ?: 0).toString()
        if (user.profileImage != null && user.profileImage != "") {
            binding.userPhotoImageView.load(user.profileImage) {
                error(R.drawable.user)
            }
        }else{
            binding.userPhotoImageView.load(R.drawable.user)
        }
        if(user.quotes!!.isEmpty()){
            binding.noQuoteFoundContainer.visibility = View.VISIBLE
        }
        homeAdapter.setData(user.quotes)
    }

    private fun setupClickListeners() {
        binding.followButton.setOnClickListener {
            findNavController().navigate(R.id.action_myProfileFragment_to_editUserFragment)
        }
        binding.loginButton.setOnClickListener{
            authViewModel.logout()
            startActivity(Intent(requireActivity(),StartActivity::class.java))
            requireActivity().finish()
        }
    }
    private fun setFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "deletedQuote",
            viewLifecycleOwner
        ) { requestKey: String, deletedQuote: Bundle ->
            userViewModel.notifyQuoteRemoved(deletedQuote["deletedQuote"] as Quote)
        }
        parentFragmentManager.setFragmentResultListener(
            "updatedQuote",
            viewLifecycleOwner
        ) { s: String, updatedQuote: Bundle ->
            userViewModel.notifyQuoteUpdated((updatedQuote["updatedQuote"] as Quote))
        }
    }

    private fun setupRecyclerView() {
        homeAdapter.currentUserId = authViewModel.currentUserId
        homeAdapter.onLikeClickListener = { quote ->
            quoteViewModel.toggleLike(quote)
        }
        homeAdapter.onQuoteOptionsClickListener = { quote ->
            val action = MyProfileFragmentDirections.actionGlobalQuoteOptionsFragment(quote)
            findNavController().navigate(action)
        }
        homeAdapter.OnBookClickListener = {
            val action =
                MyProfileFragmentDirections.actionMyProfileFragmentToBookProfileFragment(it)
            findNavController().navigate(action)
        }
        binding.userQuotesRecyclerView.adapter = homeAdapter
        binding.userQuotesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.profileContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                val notReachedBottom = v.canScrollVertically(1)
                if (!notReachedBottom && !paginationLoading && !paginationFinished) {
                    currentPage++
                    userViewModel.getMoreUserQuotes(page = currentPage)
                }
            }
        })
    }


}
