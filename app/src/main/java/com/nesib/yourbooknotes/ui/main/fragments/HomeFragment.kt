package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentHomeBinding
import com.nesib.yourbooknotes.databinding.FullPostLayoutBinding
import com.nesib.yourbooknotes.ui.main.MainActivity

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding:FragmentHomeBinding
    private lateinit var fullPostLayoutBinding: FullPostLayoutBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        fullPostLayoutBinding = FullPostLayoutBinding.inflate(layoutInflater)


        val adapter = HomeAdapter()
        adapter.OnBookClickListener = {
            findNavController().navigate(R.id.action_homeFragment_to_bookProfileFragment)
        }
        adapter.OnUserImageViewClickListener = {
            findNavController().navigate(R.id.action_homeFragment_to_userProfileFragment)
        }
        adapter.OnUsernameTextViewClickListener = {
            findNavController().navigate(R.id.action_homeFragment_to_userProfileFragment)
        }
        binding.homeRecyclerView.adapter = adapter
        binding.homeRecyclerView.layoutManager = LinearLayoutManager(context)


        binding.homeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(dy>0){

                }
            }

        })


    }
}