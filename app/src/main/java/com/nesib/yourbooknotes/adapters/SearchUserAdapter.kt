package com.nesib.yourbooknotes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.SearchUserLayoutBinding
import com.nesib.yourbooknotes.models.User

class SearchUserAdapter : RecyclerView.Adapter<SearchUserAdapter.ViewHolder>() {
    private var userList: List<User> = emptyList()
    var onUserClickListener:((User)->Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = SearchUserLayoutBinding.bind(itemView)
        fun bindData(user: User) {
            binding.apply {
                searchUserName.text = user.username
                if(user.profileImage != null && user.profileImage != ""){
                    searchUserImage.load(user.profileImage)
                }else{
                    searchUserImage.load(R.drawable.user)

                }

                root.setOnClickListener {
                    onUserClickListener!!(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchUserAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.search_user_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchUserAdapter.ViewHolder, position: Int) {
        holder.bindData(userList[position])
    }

    fun setData(newUserList: List<User>) {
        userList = newUserList
        notifyDataSetChanged()
    }

    override fun getItemCount() = userList.size
}