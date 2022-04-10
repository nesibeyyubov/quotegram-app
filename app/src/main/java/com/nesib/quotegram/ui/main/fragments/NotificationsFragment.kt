package com.nesib.quotegram.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.NotificationAdapter
import com.nesib.quotegram.databinding.FragmentNotificationsBinding
import com.nesib.quotegram.models.Notification
import com.nesib.quotegram.ui.on_boarding.StartActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.NotificationViewModel
import com.nesib.quotegram.utils.*
import com.nesib.quotegram.utils.Constants.TEXT_DIRECT_TO_LOGIN
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {
    private lateinit var binding: FragmentNotificationsBinding
    private val notificationAdapter by lazy { NotificationAdapter() }
    private val notificationViewModel: NotificationViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })

    private var currentNotifications: List<Notification>? = null
    private var currentPage = 1
    private var paginatingFinished = false
    private var paginationLoading = false
    private var comingBackFromQuote = false

    private var clearAllMenuItem: MenuItem? = null
    private var clearAllTextActionView: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNotificationsBinding.bind(view)
        setHasOptionsMenu(true)
        setupUi()
        setupRecyclerView()
        setupClickListeners()
        subscribeObservers()
        notificationViewModel.getNotifications()
    }

    private fun setupUi() {
        if (!authViewModel.isAuthenticated) {
            binding.notSignedinContainer.visibility = View.VISIBLE
            binding.loginButton.setOnClickListener {
                authViewModel.logout()
                val intent = Intent(requireActivity(), StartActivity::class.java)
                intent.putExtra(TEXT_DIRECT_TO_LOGIN, true)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    private fun subscribeObservers() {
        notificationViewModel.clearNotifications.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    clearAllMenuItem?.actionView = clearAllTextActionView
                    currentNotifications = emptyList()
                    notificationAdapter.setData(currentNotifications!!)
                    if (currentNotifications!!.isEmpty()) {
                        paginatingFinished = true
                        binding.noNotificationsContainer.visibility = View.VISIBLE
                    }
                }
                is DataState.Loading -> {
                    clearAllMenuItem?.setActionView(R.layout.progress_bar_layout)
                }
                is DataState.Fail -> {
                    clearAllMenuItem?.actionView = clearAllTextActionView
                    showToast(it.message)
                }
            }
        }
        notificationViewModel.notifications.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> with(binding) {
                    if (paginationProgressBar.visibility == View.VISIBLE) {
                        paginationProgressBar.invisible()
                    }
                    paginationLoading = false
                    if (currentNotifications?.size == it.data!!.notifications!!.toList().size && !comingBackFromQuote) {
                        paginatingFinished = true
                    }
                    comingBackFromQuote = false
                    currentNotifications = it.data.notifications!!.toList()
                    progressBar.invisible()
                    notificationAdapter.setData(currentNotifications!!)
                    if (currentNotifications!!.isEmpty()) {
                        paginatingFinished = true
                        noNotificationsContainer.visible()
                    }
                }
                is DataState.Loading -> with(binding) {
                    noNotificationsContainer.invisible()
                    failContainer.gone()
                    if (paginationLoading) paginationProgressBar.visible() else progressBar.visible()
                }
                is DataState.Fail -> with(binding) {
                    failContainer.visible()
                    failMessage.text = it.message
                    if (paginationProgressBar.visibility == View.VISIBLE) {
                        paginationProgressBar.invisible()
                    }
                    paginationLoading = false
                    progressBar.invisible()
                    showToast(it.message)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter.onNotificationClickListener = {
            val action =
                NotificationsFragmentDirections.actionNotificationsFragmentToQuoteFragment(it)
            findNavController().navigate(action)
        }
        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.notificationsRecyclerView.apply {
            adapter = notificationAdapter
            layoutManager = mLayoutManager
            this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (!paginatingFinished && mLayoutManager.findLastCompletelyVisibleItemPosition() == (currentNotifications!!.size - 1) && !paginationLoading) {
                            currentPage++
                            paginationLoading = true
                            notificationViewModel.getNotifications(currentPage)
                        }
                    }
                }
            })
        }


    }

    private fun setupClickListeners() {
        binding.tryAgainButton.setOnClickListener {
            notificationViewModel.getNotifications(1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (authViewModel.currentUserId != null) {
            inflater.inflate(R.menu.notifications_menu, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear_notifications_menu_item) {
            if (currentNotifications!!.isNotEmpty()) {
                clearAllMenuItem = item
                clearAllTextActionView = clearAllMenuItem?.actionView
                notificationViewModel.clearNotifications()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}