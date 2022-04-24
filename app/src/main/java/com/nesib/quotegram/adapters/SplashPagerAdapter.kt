package com.nesib.quotegram.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.ItemSplashLayoutBinding

class SplashPagerAdapter :
    RecyclerView.Adapter<SplashPagerAdapter.ViewHolder>() {
    private val fragments = listOf(
        Data(
            title = R.string.txt_welcome,
            description = R.string.txt_splash_first,
            image = R.drawable.first
        ),
        Data(
            title = R.string.txt_explore_quotes,
            description = R.string.txt_splash_second,
            image = R.drawable.third
        ),
        Data(
            title = R.string.txt_enjoy_sharing,
            description = R.string.txt_splash_third,
            image = R.drawable.second
        )
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_splash_layout, parent, false)
        )
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemSplashLayoutBinding.bind(view)
        fun bind(data: Data) = with(binding) {
            tvTitle.text = view.context.getString(data.title)
            tvDescription.text = view.context.getString(data.description)
            ivPhone.setImageResource(data.image)
        }
    }

    override fun onBindViewHolder(holder: SplashPagerAdapter.ViewHolder, position: Int) {
        holder.bind(fragments[position])
    }

    override fun getItemCount() = fragments.size


    data class Data(
        @StringRes val title: Int,
        @StringRes val description: Int,
        @DrawableRes val image: Int
    )


}
