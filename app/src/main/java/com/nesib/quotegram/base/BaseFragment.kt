package com.nesib.quotegram.base

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

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

}