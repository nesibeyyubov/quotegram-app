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
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentMyProfileBinding
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.models.User
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.on_boarding.StartActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.QuoteViewModel
import com.nesib.quotegram.ui.viewmodels.UserViewModel
import com.nesib.quotegram.utils.*
import com.nesib.quotegram.utils.Constants.KEY_DELETED_QUOTE
import com.nesib.quotegram.utils.Constants.TEXT_DIRECT_TO_LOGIN
import com.nesib.quotegram.utils.Constants.KEY_UPDATED_QUOTE
import com.nesib.quotegram.utils.Constants.TEXT_UPDATED_USER
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyProfileFragment : BaseFragment<FragmentMyProfileBinding>(R.layout.fragment_my_profile) {
    private val homeAdapter by lazy { HomeAdapter((activity as MainActivity).dialog) }
    private val userViewModel: UserViewModel by viewModels()
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })

    private var paginationLoading = false
    private var currentPage = 1
    private var paginationFinished = false
    private var quotesSize = 0
    private var currentUser: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            binding.notSignedinContainer.visible()
        }
    }

    private fun subscribeObservers() = with(binding) {
        userViewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    failContainer.safeGone()
                    toggleProgressBar(false)
                    currentUser = it.data!!.user!!
                    quotesSize = currentUser!!.quotes!!.size
                    bindData(currentUser!!)
                }
                is DataState.Fail -> {
                    failMessage.text = it.message
                    failContainer.visible()
                    toggleProgressBar(loading = false, failed = true)
                }
                is DataState.Loading -> {
                    failContainer.safeGone()
                    toggleProgressBar(true)
                }
            }
        }
        userViewModel.userQuotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    paginationFinished = quotesSize == it.data!!.quotes.size
                    paginationLoading = false
                    homeAdapter.setData(it.data.quotes)
                    quotesSize = it.data.quotes.size
                    paginationProgressBar.invisible()
                }
                is DataState.Fail -> {
                    showToast(it.message)
                    paginationLoading = false
                }
                is DataState.Loading -> {
                    paginationLoading = true
                    paginationProgressBar.visible()
                }
            }
        }
    }

    private fun toggleProgressBar(loading: Boolean, failed: Boolean = false) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.profileContent.visibility = if (loading || failed) View.INVISIBLE else View.VISIBLE
        binding.notSignedinContainer.visibility = View.GONE
    }

    private fun bindData(user: User) = with(binding) {
        usernameTextView.text = user.username
        bioTextView.text = if (user.bio?.isNotEmpty() == true) user.bio else ""
        followerCountTextView.text = user.followers!!.size.toFormattedNumber()
        followingCountTextView.text = (user.followingUsers!!.size).toString()
        quoteCountTextView.text = (user.totalQuoteCount ?: 0).toFormattedNumber()
        if (user.profileImage != null && user.profileImage != "") {
            userPhotoImageView.load(user.profileImage) {
                error(R.drawable.user)
            }
        } else {
            userPhotoImageView.load(R.drawable.user)
        }
        if (user.quotes!!.isEmpty()) {
            noQuoteFoundContainer.visible()
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
            intent.putExtra(TEXT_DIRECT_TO_LOGIN, true)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.tryAgainButton.setOnClickListener {
            userViewModel.getUser(forced = true)
        }
    }

    private fun setFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            KEY_DELETED_QUOTE,
            viewLifecycleOwner
        ) { requestKey: String, deletedQuote: Bundle ->
            userViewModel.notifyQuoteRemoved(deletedQuote[KEY_DELETED_QUOTE] as Quote)
        }
        parentFragmentManager.setFragmentResultListener(
            KEY_UPDATED_QUOTE,
            viewLifecycleOwner
        ) { s: String, updatedQuote: Bundle ->
            userViewModel.notifyQuoteUpdated((updatedQuote[KEY_UPDATED_QUOTE] as Quote))
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
            val shareIntent = Intent.createChooser(intent, getString(R.string.share_quote))
            startActivity(shareIntent)

        }
        binding.userQuotesRecyclerView.adapter = homeAdapter
        binding.userQuotesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.profileContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                val notReachedBottom = v.canScrollVertically(1)
                if (!notReachedBottom && !paginationLoading && !paginationFinished) {
                    currentPage++
                    paginationLoading = true
                    userViewModel.getMoreUserQuotes(page = currentPage)
                }
            }
        })
    }

    override fun createBinding(view: View) = FragmentMyProfileBinding.bind(view)


}
