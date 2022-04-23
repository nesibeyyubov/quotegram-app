package com.nesib.quotegram.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.QuoteImageStyleItemBinding

class ColorBoxAdapter(val photoStyleColors: List<String>) :
    RecyclerView.Adapter<ColorBoxAdapter.ViewHolder>() {
    var onColorBoxClickedListener: ((View, String) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = QuoteImageStyleItemBinding.bind(itemView)

        init {
            binding.colorBox.setOnClickListener { view ->
                onColorBoxClickedListener?.let {
                    it(view, photoStyleColors[adapterPosition])
                }
            }
        }

        fun bindData(color: String) {
            binding.colorBox.background = ColorDrawable(Color.parseColor(color))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorBoxAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.quote_image_style_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorBoxAdapter.ViewHolder, position: Int) {
        holder.bindData(photoStyleColors[position])
    }

    override fun getItemCount() = photoStyleColors.size
}