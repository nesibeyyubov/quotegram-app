package com.nesib.yourbooknotes.ui.main.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentEditProfileBinding
import com.nesib.yourbooknotes.databinding.FragmentUserProfileBinding

class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {
    private val adapter by lazy { HomeAdapter() }
    private lateinit var binding: FragmentUserProfileBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserProfileBinding.bind(view)

        binding.followButton.setOnClickListener {
            findNavController().navigate(R.id.action_userProfileFragment_to_editUserFragment)
        }
        binding.userQuotesRecyclerView.adapter = adapter
        binding.userQuotesRecyclerView.layoutManager = LinearLayoutManager(context)

    }


}
