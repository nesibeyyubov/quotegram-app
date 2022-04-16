package com.nesib.quotegram.ui.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.LayoutGoogleButtonBinding
import com.nesib.quotegram.databinding.LayoutProgressBarButtonBinding
import com.nesib.quotegram.utils.gone
import com.nesib.quotegram.utils.visible

class GoogleButton @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RelativeLayout(context, attributeSet, defStyleAttr) {
    private var text: String = ""

    private var binding: LayoutGoogleButtonBinding = LayoutGoogleButtonBinding.bind(
        LayoutInflater.from(this.context)
            .inflate(R.layout.layout_google_button, this, true)
    )

    init {
        this.isClickable = true
        this.isFocusable = true
        val attributes =
            context.theme.obtainStyledAttributes(attributeSet, R.styleable.GoogleButton, 0, 0)
        text = attributes.getString(R.styleable.GoogleButton_text) ?: ""
        initUi()
    }

    private fun initUi() = with(binding) {
        tvText.text = text
    }

    fun showLoading() = with(binding) {
        root.isEnabled = false
        progressBar.visible()
        tvText.gone()
    }

    fun hideLoading() = with(binding) {
        progressBar.gone()
        tvText.visible()
        root.isEnabled = true
    }


}