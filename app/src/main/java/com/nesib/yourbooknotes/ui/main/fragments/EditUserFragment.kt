package com.nesib.yourbooknotes.ui.main.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FragmentEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditUserFragment : Fragment(R.layout.fragment_edit_profile) {
    private lateinit var binding: FragmentEditProfileBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditProfileBinding.bind(view)
    }
}