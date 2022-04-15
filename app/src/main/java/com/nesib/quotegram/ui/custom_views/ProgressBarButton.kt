package com.nesib.quotegram.ui.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.LayoutProgressBarButtonBinding
import com.nesib.quotegram.utils.gone
import com.nesib.quotegram.utils.visible

class ProgressBarButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(context, attributeSet, defStyleAttr) {

    private var binding: LayoutProgressBarButtonBinding = LayoutProgressBarButtonBinding.bind(
        LayoutInflater.from(this.context)
            .inflate(R.layout.layout_progress_bar_button, null, true)
    )

    fun setOnClickListener(listener: () -> Unit) {
        binding.root.setOnClickListener { listener.invoke() }
    }

    fun showLoading() = with(binding) {
        progressBar.visible()
        tvNext.gone()
    }

    fun hideLoading() {
        binding.progressBar.gone()
    }


}