package com.nesib.yourbooknotes.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.NotificationAdapter
import com.nesib.yourbooknotes.databinding.FragmentNotificationsBinding
import com.nesib.yourbooknotes.models.Notification
import com.nesib.yourbooknotes.ui.on_boarding.StartActivity
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.NotificationViewModel
import com.nesib.yourbooknotes.utils.DataState
import com.nesib.yourbooknotes.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {
    private lateinit var binding: FragmentNotificationsBinding
    private val notificationAdapter by lazy { NotificationAdapter() }
    private val notificationViewModel: NotificationViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels({requireActivity()})

    private var currentNotifications: List<Notification>? = null
    private var currentPage = 1
    private var paginatingFinished = false
    private var paginationLoading = false
    private var comingBackFromQuote = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNotificationsBinding.bind(view)
        setupUi()
        setupRecyclerView()
        setupClickListeners()
        subscribeObservers()
    }

    private fun setupUi() {
        if(authViewModel.isAuthenticated){
            if(currentNotifications == null){
                notificationViewModel.getNotifications()
            }else{
                comingBackFromQuote = true
                notificationAdapter.setData(currentNotifications!!)
            }
        }else{
            binding.notSignedinContainer.visibility = View.VISIBLE
            binding.loginButton.setOnClickListener {
                authViewModel.logout()
                startActivity(Intent(requireActivity(), StartActivity::class.java))
                requireActivity().finish()
            }
        }
    }
    private fun subscribeObservers() {
        notificationViewModel.notifications.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if(binding.paginationProgressBar.visibility == View.VISIBLE){
                        binding.paginationProgressBar.visibility = View.INVISIBLE
                    }
                    paginationLoading = false
                    if(currentNotifications?.size == it.data!!.notifications!!.toList().size && !comingBackFromQuote){
                        paginatingFinished = true
                    }
                    comingBackFromQuote = false
                    currentNotifications = it.data.notifications!!.toList()
                    binding.progressBar.visibility = View.INVISIBLE
                    notificationAdapter.setData(currentNotifications!!)
                    if(currentNotifications!!.isEmpty()){
                        paginatingFinished = true
                        binding.noNotificationsContainer.visibility = View.VISIBLE
                    }
                }
                is DataState.Loading -> {
                    binding.failContainer.visibility = View.GONE
                    if(paginationLoading){
                        binding.paginationProgressBar.visibility = View.VISIBLE
                    }else{
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
                is DataState.Fail -> {
                    binding.failContainer.visibility = View.VISIBLE
                    binding.failMessage.text = it.message
                    if(binding.paginationProgressBar.visibility == View.VISIBLE){
                        binding.paginationProgressBar.visibility = View.INVISIBLE
                    }
                    paginationLoading = false
                    binding.progressBar.visibility = View.INVISIBLE
                    showToast(it.message)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter.onNotificationClickListener={
            val action = NotificationsFragmentDirections.actionNotificationsFragmentToQuoteFragment(it)
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
}