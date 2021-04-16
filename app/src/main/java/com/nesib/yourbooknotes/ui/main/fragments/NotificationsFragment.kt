package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.NotificationAdapter
import com.nesib.yourbooknotes.databinding.FragmentNotificationsBinding
import com.nesib.yourbooknotes.ui.viewmodels.NotificationViewModel
import com.nesib.yourbooknotes.utils.DataState
import com.nesib.yourbooknotes.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {
    private lateinit var binding:FragmentNotificationsBinding
    private val notificationAdapter by lazy { NotificationAdapter() }
    private val notificationViewModel:NotificationViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNotificationsBinding.bind(view)
        setupUi()
        setupRecyclerView()
        setupClickListeners()
        subscribeObservers()
        notificationViewModel.getNotifications()
    }

    private fun setupUi(){}
    private fun subscribeObservers(){
        notificationViewModel.notifications.observe(viewLifecycleOwner){
            when(it){
                is DataState.Success->{
                    if(notificationViewModel.shouldReadNotifications){
                        notificationViewModel.readNotifications()
                    }
                    binding.progressBar.visibility = View.INVISIBLE
                    notificationAdapter.setData(it.data!!.notifications!!.toList())
                }
                is DataState.Loading->{
                    binding.progressBar.visibility = View.VISIBLE
                }
                is DataState.Fail->{
                    binding.progressBar.visibility = View.INVISIBLE
                    showToast(it.message)
                }
            }
        }
    }
    private fun setupRecyclerView(){
        binding.notificationsRecyclerView.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    private fun setupClickListeners(){}
}