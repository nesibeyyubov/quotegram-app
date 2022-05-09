package com.nesib.quotegram.base

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.nesib.quotegram.R

abstract class BaseFragment<VBinding : ViewBinding>(layoutResource: Int) :
    Fragment(layoutResource) {

    private var _binding: VBinding? = null
    val binding: VBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = createBinding(view)
    }

    abstract fun createBinding(view: View): VBinding

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun navigateTo(screenId: Int) {
        findNavController().navigate(screenId)
    }

    fun navigateBack(){
        findNavController().popBackStack()
    }

    fun getCurrentScreen(): Int? {
        return findNavController().currentDestination?.id
    }

}