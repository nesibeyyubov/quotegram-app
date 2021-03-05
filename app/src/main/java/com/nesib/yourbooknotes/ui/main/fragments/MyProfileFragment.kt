package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentMyProfileBinding
import com.nesib.yourbooknotes.ui.main.MainActivity
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState

class MyProfileFragment : Fragment(R.layout.fragment_my_profile) {
    private lateinit var binding: FragmentMyProfileBinding

    private val adapter by lazy { HomeAdapter() }
    private val userViewModel: UserViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyProfileBinding.bind(view)
        (activity as MainActivity).supportActionBar?.title = ""

        setupClickListeners()
        setupRecyclerView()
        subscribeObservers()

        userViewModel.getUser()

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
