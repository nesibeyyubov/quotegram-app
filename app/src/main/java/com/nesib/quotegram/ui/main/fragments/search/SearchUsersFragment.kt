package com.nesib.quotegram.ui.main.fragments.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.SearchUserAdapter
import com.nesib.quotegram.databinding.FragmentSearchUsersBinding
import com.nesib.quotegram.ui.viewmodels.SharedViewModel
import com.nesib.quotegram.ui.viewmodels.UserViewModel
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchUsersFragment : Fragment(R.layout.fragment_search_users) {
    private val adapter by lazy { SearchUserAdapter() }
    private lateinit var binding: FragmentSearchUsersBinding
    private val userViewModel: UserViewModel by viewModels({ requireParentFragment() })
    private val sharedViewModel: SharedViewModel by viewModels({ requireActivity() })
    private var searchViewTextChanged = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchUsersBinding.bind(view)
        setupRecyclerView()
        subscribeObservers()
    }

    private fun setupRecyclerView() {
        adapter.onUserClickListener = { user ->
            val action = SearchFragmentDirections.actionSearchFragmentToUserProfileFragment(user.id)
            findNavController().navigate(action)
        }
        binding.searchUsersRecyclerView.adapter = adapter
        binding.searchUsersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeObservers() {
        userViewModel.users.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if (it.data!!.users.isEmpty()) {
                        binding.noUserFound.visibility = View.VISIBLE
                    }
                    adapter.setData(it.data.users)
                }
                is DataState.Fail -> {
                    showToast(it.message)
                }
                is DataState.Loading -> {

                }
            }
        }
        sharedViewModel.searchTextUser.observe(viewLifecycleOwner) { text ->
            if (text.isNotEmpty()) {
                searchViewTextChanged = true
                Handler(Looper.getMainLooper())
                    .postDelayed({
                        userViewModel.getUsers(text)
                    }, 300)
            }
        }
    }

    private fun toggleProgressBar(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
        binding.searchUsersRecyclerView.visibility = if (loading) View.INVISIBLE else View.VISIBLE
    }

}