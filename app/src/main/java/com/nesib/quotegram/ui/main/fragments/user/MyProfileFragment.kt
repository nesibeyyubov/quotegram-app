package com.nesib.quotegram.ui.main.fragments.user

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.HomeAdapter
import com.nesib.quotegram.databinding.FragmentMyProfileBinding
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.models.User
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.on_boarding.StartActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.QuoteViewModel
import com.nesib.quotegram.ui.viewmodels.UserViewModel
import com.nesib.quotegram.utils.Constants.TEXT_DELETED_QUOTE
import com.nesib.quotegram.utils.Constants.TEXT_DIRECT_TO_LOGIN
import com.nesib.quotegram.utils.Constants.TEXT_UPDATED_QUOTE
import com.nesib.quotegram.utils.Constants.TEXT_UPDATED_USER
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.showToast
import com.nesib.quotegram.utils.toFormattedNumber
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyProfileFragment : Fragment(R.layout.fragment_my_profile) {
    private lateinit var binding: FragmentMyProfileBinding

    private val homeAdapter by lazy { HomeAdapter((activity as MainActivity).dialog) }
    private val userViewModel: UserViewModel by viewModels()
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })

    private var paginationLoading = false
    private var currentPage = 1
    private var paginationFinished = false
    private var currentUserQuotes: MutableList<Quote>? = null
    private var currentUser: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyProfileBinding.bind(view)
        setupClickListeners()
        setupRecyclerView()
        subscribeObservers()
        getUser()
        setFragmentResultListener()
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
                    if (binding.failContainer.visibility == View.VISIBLE) {
                        binding.failContainer.visibility = View.GONE
                    }
                    toggleProgressBar(false)
                    currentUser = it.data!!.user!!
                    currentUserQuotes = currentUser!!.quotes!!.toMutableList()
                    bindData(currentUser!!)
                }
                is DataState.Fail -> {
                    binding.failMessage.text = it.message
                    binding.failContainer.visibility = View.VISIBLE
                    toggleProgressBar(false, true)
                }
                is DataState.Loading -> {
                    if (binding.failContainer.visibility == View.VISIBLE) {
                        binding.failContainer.visibility = View.GONE
                    }
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
                    showToast(it.message!!)
                    paginationLoading = false
                }
                is DataState.Loading -> {
                    binding.paginationProgressBar.visibility = View.VISIBLE
                    paginationLoading = true
                }
            }
        }
    }

    private fun toggleProgressBar(loading: Boolean, failed: Boolean = false) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.profileContent.visibility = if (loading || failed) View.INVISIBLE else View.VISIBLE
        binding.notSignedinContainer.visibility = View.GONE
    }

    private fun bindData(user: User) {
        binding.usernameTextView.text = user.username
        binding.bioTextView.text = if (user.bio!!.isNotEmpty()) user.bio else "No bio"
        binding.followerCountTextView.text = user.followers!!.size.toFormattedNumber()
        binding.followingCountTextView.text =
            (user.followingUsers!!.size).toString()
        binding.quoteCountTextView.text = (user.totalQuoteCount ?: 0).toFormattedNumber()
        if (user.profileImage != null && user.profileImage != "") {
            binding.userPhotoImageView.load(user.profileImage) {
                error(R.drawable.user)
            }
        } else {
            binding.userPhotoImageView.load(R.drawable.user)
        }
        if (user.quotes!!.isEmpty()) {
            binding.noQuoteFoundContainer.visibility = View.VISIBLE
        }
        homeAdapter.setData(user.quotes)
    }

    private fun setupClickListeners() {
        binding.editUserButton.setOnClickListener {
            val action = MyProfileFragmentDirections.actionMyProfileFragmentToEditUserFragment()
            action.user = currentUser
            findNavController().navigate(action)
        }
        binding.loginButton.setOnClickListener {
            authViewModel.logout()
            val intent = Intent(requireActivity(), StartActivity::class.java)
            intent.putExtra(TEXT_DIRECT_TO_LOGIN,true)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.tryAgainButton.setOnClickListener {
            userViewModel.getUser(forced = true)
        }
    }

    private fun setFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            TEXT_DELETED_QUOTE,
            viewLifecycleOwner
        ) { requestKey: String, deletedQuote: Bundle ->
            userViewModel.notifyQuoteRemoved(deletedQuote[TEXT_DELETED_QUOTE] as Quote)
            binding.quoteCountTextView.text = (binding.quoteCountTextView.text.toString().toInt() - 1).toString()
        }
        parentFragmentManager.setFragmentResultListener(
            TEXT_UPDATED_QUOTE,
            viewLifecycleOwner
        ) { s: String, updatedQuote: Bundle ->
            userViewModel.notifyQuoteUpdated((updatedQuote[TEXT_UPDATED_QUOTE] as Quote))
        }

        parentFragmentManager.setFragmentResultListener(
            TEXT_UPDATED_USER,
            viewLifecycleOwner
        ) { s: String, updatedUser: Bundle ->
            binding.apply {
                currentUser = updatedUser[TEXT_UPDATED_USER] as User
                bindData(currentUser!!)
            }
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
        homeAdapter.onDownloadClickListener = { quote ->
            val action = MyProfileFragmentDirections.actionGlobalDownloadQuoteFragment(
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
            val shareIntent = Intent.createChooser(intent,"Share Quote")
            startActivity(shareIntent)

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
