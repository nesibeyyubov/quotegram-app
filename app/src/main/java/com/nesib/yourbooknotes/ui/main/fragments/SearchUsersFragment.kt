package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.SearchUserAdapter
import com.nesib.yourbooknotes.databinding.FragmentSearchUsersBinding
import com.nesib.yourbooknotes.ui.viewmodels.UserViewModel
import com.nesib.yourbooknotes.utils.DataState
import com.nesib.yourbooknotes.utils.IUsersNotifer
import com.nesib.yourbooknotes.utils.Utils

class SearchUsersFragment : Fragment(R.layout.fragment_search_users),IUsersNotifer {
    private val adapter by lazy { SearchUserAdapter() }
    private lateinit var binding: FragmentSearchUsersBinding
    private val userViewModel:UserViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Utils.usersNotifier = this
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchUsersBinding.bind(view)
        setupRecyclerView()
        subscribeObservers()
        userViewModel.getUsers()
    }

    private fun setupRecyclerView(){
        binding.searchUsersRecyclerView.adapter = adapter
        binding.searchUsersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeObservers(){
        userViewModel.users.observe(viewLifecycleOwner){
            when(it){
                is DataState.Success->{
                    toggleProgressBar(false)
                    adapter.setData(it.data!!.users)
                }
                is DataState.Fail->{
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
        binding.searchUsersRecyclerView.visibility = if(loading) View.GONE else View.VISIBLE
    }

    override fun notify(text: String) {
        Log.d("mytag", "user notify: $text")
    }
}