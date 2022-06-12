package com.nesib.quotegram.ui.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.nesib.quotegram.R

class SingleSelectBottomView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 1
) : LinearLayout(context, attributeSet, defStyleAttr) {

    private var selectedState = Item.None

    enum class Item { Image, Text, Color, None }

    var onImageClick = {}
    var onTextClick = {}
    var onColorClick = {}


    private val items by lazy {
        mutableListOf(
            binding.itemColor,
            binding.itemImage,
            binding.itemTextStyle
        )
    }

    private val binding =
        com.nesib.quotegram.databinding.SingleSelectBottomViewBinding.inflate(
            LayoutInflater.from(
                context
            ), this, true
        )

    init {
        initClickListeners()
    }

    private fun initClickListeners() = with(binding) {
        itemColor.setOnClickListener {
            setState(Item.Color)
            onColorClick()
        }
        itemImage.setOnClickListener {
            setState(Item.Image)
            onImageClick()
        }
        itemTextStyle.setOnClickListener {
            setState(Item.Text)
            onTextClick()
        }
    }

    private fun setState(state: Item) {
        selectedState = state
        setSelectedStyle()
    }

    private fun setSelectedStyle() = with(binding) {
        val blue = ContextCompat.getColor(context, R.color.blue)
        val default = ContextCompat.getColor(context, R.color.colorPrimaryText)
        items.forEach {
            it.setTextColor(default)
            it.compoundDrawables[1]?.setTint(default)
        }
        val item: TextView? = when (selectedState) {
            Item.Text -> itemTextStyle
            Item.Image -> itemImage
            Item.Color -> itemColor
            Item.None -> null
        }
        item?.setTextColor(blue)
        item?.compoundDrawablesRelative?.run { this[1]?.setTint(blue) }
    }


}