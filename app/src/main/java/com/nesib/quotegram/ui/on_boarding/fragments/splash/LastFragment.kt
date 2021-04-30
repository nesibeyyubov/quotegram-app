package com.nesib.quotegram.ui.on_boarding.fragments.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.FragmentLastBinding

class LastFragment : Fragment(R.layout.fragment_last) {
    private lateinit var binding: FragmentLastBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLastBinding.bind(view)
    }



}