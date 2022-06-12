package com.nesib.quotegram.ui.main.fragments.user

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.HomeAdapter
import com.nesib.quotegram.base.BaseFragment
import com.nesib.quotegram.databinding.FragmentUserProfileBinding
import com.nesib.quotegram.databinding.ReportDialogBinding
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.models.User
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.QuoteViewModel
import com.nesib.quotegram.ui.viewmodels.ReportViewModel
import com.nesib.quotegram.ui.viewmodels.UserViewModel
import com.nesib.quotegram.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileFragment :
    BaseFragment<FragmentUserProfileBinding>() {

    private val homeAdapter by lazy { HomeAdapter((activity as MainActivity).dialog) }
    private val userViewModel: UserViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val reportViewModel: ReportViewModel by viewModels()
    private val args by navArgs<UserProfileFragmentArgs>()

    private var paginatingFinished = false
    private var paginationLoading = false
    private var currentPage = 1
    private var quotesSize = 0
    private var currentUser: User? = null

    private var makeSureDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupRecyclerView()
        subscribeObservers()
        setHasOptionsMenu(true)

        userViewModel.getUser(args.userId)
    }


    private fun bindData(user: User) = with(binding) {
        usernameTextView.text = user.username
        bioTextView.text = if (user.bio!!.isNotEmpty()) user.bio else ""
        followerCountTextView.text = user.followers!!.size.toFormattedNumber()
        followingCountTextView.text =
            (user.followingUsers!!.size).toFormattedNumber()
        quoteCountTextView.text = (user.totalQuoteCount ?: 0).toFormattedNumber()
        if (user.profileImage != null && user.profileImage != "") {
            userPhotoImageView.load(user.profileImage) {
                error(R.drawable.user)
            }
        } else {
            userPhotoImageView.load(R.drawable.user)
        }
        if (user.quotes!!.isEmpty()) {
            noQuoteFoundContainer.visibility = View.VISIBLE
        }
        toggleFollowButtonStyle(user.followers!!.contains(authViewModel.currentUserId))

        homeAdapter.setData(user.quotes!!)
    }

    private fun toggleFollowButtonStyle(following: Boolean) = with(binding) {
        followButton.setBackgroundResource(if (following) R.drawable.add_quote_from_this_book_bg else R.drawable.follow_button_bg)
        followButtonTextView.text =
            if (following) getString(R.string.txt_following) else getString(R.string.txt_follow)
        followButtonTextView.setTextColor(
            if (following) ContextCompat.getColor(
                requireContext(),
                R.color.blue
            ) else ContextCompat.getColor(requireContext(), R.color.white)
        )
    }

    private fun hideRefreshLayoutProgress() {
        if (binding.refreshLayout.isRefreshing) binding.refreshLayout.isRefreshing = false
    }

    private fun subscribeObservers() = with(binding) {
        userViewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    hideRefreshLayoutProgress()
                    failContainer.safeGone()
                    progressBar.gone()
                    profileContent.visible()
                    currentUser = it.data!!.user!!
                    quotesSize = currentUser!!.quotes!!.size
                    bindData(currentUser!!)
                }
                is DataState.Fail -> {
                    hideRefreshLayoutProgress()
                    failContainer.visible()
                    failMessage.text = it.message
                    progressBar.gone()
                    profileContent.invisible()
                }
                is DataState.Loading -> {
                    failContainer.safeGone()
                    profileContent.gone()
                    progressBar.visible()
                }
            }
        }
        userViewModel.userQuotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    paginatingFinished = quotesSize == it.data!!.quotes.size
                    paginationProgressBar.safeInvisible()
                    paginationLoading = false
                    homeAdapter.setData(it.data.quotes)
                    quotesSize = it.data.quotes.size
                }
                is DataState.Fail -> {
                    paginationProgressBar.invisible()
                    showToast(it.message)
                    paginationLoading = false
                }
                is DataState.Loading -> {
                    if (paginationLoading) {
                        paginationProgressBar.visible()
                    }
                }
            }
        }
        reportViewModel.report.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    makeSureDialog?.dismiss()
                    showToast(it.data!!.message)
                }
                is DataState.Fail -> {
                    makeSureDialog?.dismiss()
                    showToast(it.message)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.refreshLayout.setOnRefreshListener {
            userViewModel.getUser(args.userId, forced = true)
        }
        binding.followButton.setOnClickListener {
            if (authViewModel.currentUserId == null) {
                (activity as MainActivity).showAuthenticationDialog()
                return@setOnClickListener
            }
            currentUser?.let { user ->
                val followers = user.followers!!.toMutableList()
                if (!user.followers!!.contains(authViewModel.currentUserId)) {
                    followers.add(authViewModel.currentUserId!!)
                    toggleFollowButtonStyle(true)
                    binding.followerCountTextView.text =
                        (binding.followerCountTextView.text.toString()
                            .toInt() + 1).toString()
                } else {
                    followers.remove(authViewModel.currentUserId!!)
                    toggleFollowButtonStyle(false)
                    binding.followerCountTextView.text =
                        (binding.followerCountTextView.text.toString()
                            .toInt() - 1).toString()
                }
                user.followers = followers.toList()
                userViewModel.followOrUnFollowUser(user)
            }

        }
        binding.tryAgainButton.setOnClickListener {
            userViewModel.getUser(args.userId, forced = true)
        }
    }

    private fun setupRecyclerView() {
        homeAdapter.currentUserId = authViewModel.currentUserId
        homeAdapter.onLikeClickListener = { quote ->
            quoteViewModel.toggleLike(quote)
        }
        homeAdapter.onQuoteOptionsClickListener = { quote ->
            val action = UserProfileFragmentDirections.actionGlobalQuoteOptionsFragment(quote)
            findNavController().navigate(action)
        }
        homeAdapter.onDownloadClickListener = { quote ->
            val action = UserProfileFragmentDirections.actionGlobalDownloadQuoteFragment(quote)
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
        binding.userQuotesRecyclerView.adapter = homeAdapter
        binding.userQuotesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.profileContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                val notReachedBottom = v.canScrollVertically(1)

                if (!notReachedBottom && !paginationLoading && quotesSize >= 2 && !paginatingFinished) {
                    currentPage++
                    paginationLoading = true
                    userViewModel.getMoreUserQuotes(args.userId, currentPage)
                }
            }
        })
    }

    private fun showMakeSureDialogForReport() {
        val view = layoutInflater.inflate(R.layout.report_dialog, binding.root, false)
        val binding = ReportDialogBinding.bind(view)
        makeSureDialog = AlertDialog.Builder(requireContext()).setView(binding.root).create()
        makeSureDialog!!.show()

        binding.apply {
            notNowButton.setOnClickListener { makeSureDialog!!.dismiss() }
            reportButton.setOnClickListener {
                makeSureDialog!!.setCancelable(false)
                reportProgressBar.invisible()
                reportButton.invisible()
                notNowButton.isEnabled = false
                reportViewModel.reportUser(authViewModel.currentUserId ?: "", args.userId)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.report_user_menu_item) {
            showMakeSureDialogForReport()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserProfileBinding = FragmentUserProfileBinding.inflate(inflater, container, false)


}
