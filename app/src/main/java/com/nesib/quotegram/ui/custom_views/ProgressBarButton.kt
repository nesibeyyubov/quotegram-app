package com.nesib.quotegram.ui.custom_views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
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
    var text: String = ""
        set(value) {
            field = value
            binding.tvNext.text = value
        }

    private var binding: LayoutProgressBarButtonBinding = LayoutProgressBarButtonBinding.bind(
        LayoutInflater.from(this.context)
            .inflate(R.layout.layout_progress_bar_button, this, true)
    )

    init {
        this.isClickable = true
        this.isFocusable = true
        val attributes =
            context.theme.obtainStyledAttributes(attributeSet, R.styleable.ProgressBarButton, 0, 0)
        text = attributes.getString(R.styleable.ProgressBarButton_text) ?: ""
    }


    fun showLoading() = with(binding) {
        root.isEnabled = false
        progressBar.visible()
        tvNext.gone()
    }

    fun hideLoading() = with(binding) {
        progressBar.gone()
        tvNext.visible()
        root.isEnabled = true
    }


}