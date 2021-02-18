package com.nesib.yourbooknotes.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.RecyclerView
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FullPostLayoutBinding

class HomeAdapter : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    var OnUsernameTextViewClickListener : (()->Unit)? = null
    var OnUserImageViewClickListener : (()->Unit)? = null
    var OnBookClickListener : (()->Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        init {
            val binding = FullPostLayoutBinding.bind(itemView)
            binding.usernameTextView.setOnClickListener(this)
            binding.userphotoImageView.setOnClickListener(this)
            binding.viewBookBtn.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when(v?.id){
                R.id.username_textView -> {
                    Log.d("mytag", "onClick: ")
                    OnUsernameTextViewClickListener!!()
                }
                R.id.userphoto_imageView ->{
                    OnUserImageViewClickListener!!()
                }
                R.id.viewBookBtn -> {
                    OnBookClickListener!!()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.full_post_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeAdapter.ViewHolder, position: Int) {

    }

    override fun getItemCount() = 6
}