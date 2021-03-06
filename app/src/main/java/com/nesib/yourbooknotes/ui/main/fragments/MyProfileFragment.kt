package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentMyProfileBinding
import com.nesib.yourbooknotes.models.User
import com.nesib.yourbooknotes.ui.main.MainActivity
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState

class MyProfileFragment : Fragment(R.layout.fragment_my_profile) {
    private lateinit var binding: FragmentMyProfileBinding

    private val adapter by lazy { HomeAdapter() }
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var authViewModel:AuthViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyProfileBinding.bind(view)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        setupClickListeners()
        setupRecyclerView()
        subscribeObservers()
        getUser()
    }
    private fun getUser(){
        if(authViewModel.isAuthenticated()){
            userViewModel.getUser()
        }else{
            binding.notSignedinContainer.visibility = View.VISIBLE
        }
    }

    private fun subscribeObservers() {
        userViewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    toggleProgressBar(false)
                    val user = it.data!!.user!!
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
    }

    private fun toggleProgressBar(loading:Boolean){
        binding.progressBar.visibility = if(loading) View.VISIBLE else View.GONE
        binding.profileContent.visibility = if(loading) View.GONE else View.VISIBLE
        binding.notSignedinContainer.visibility = View.GONE
    }

    private fun bindData(user:User){
        binding.usernameTextView.text = user.username
        binding.bioTextView.text = if (user.bio!!.isNotEmpty()) user.bio else "No bio"
        binding.followerCountTextView.text = user.followers!!.size.toString()
        binding.followingCountTextView.text =
            (user.followingUsers!!.size + user.followingBooks!!.size).toString()
        binding.quoteCountTextView.text = user.quotes!!.size.toString()
        adapter.setData(user.quotes)
    }

    private fun setupClickListeners() {
        binding.followButton.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_editUserFragment)
        }
    }

    private fun setupRecyclerView() {
        binding.userQuotesRecyclerView.adapter = adapter
        binding.userQuotesRecyclerView.layoutManager = LinearLayoutManager(context)
    }


}
