package com.nesib.yourbooknotes.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FullPostLayoutBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.utils.Constants.API_URL
import com.nesib.yourbooknotes.utils.DiffUtilCallback

class HomeAdapter : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    var quoteList = emptyList<Quote>()

    var OnUserClickListener: ((String) -> Unit)? = null
    var OnBookClickListener: ((String) -> Unit)? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var binding: FullPostLayoutBinding = FullPostLayoutBinding.bind(itemView)

        init {
            binding.usernameTextView.setOnClickListener(this)
            binding.userphotoImageView.setOnClickListener(this)
            binding.viewBookBtn.setOnClickListener(this)
            binding.bookInfoContainer.setOnClickListener(this)
        }

        fun bindData() {
            val quote = quoteList[adapterPosition]
            binding.apply {
                usernameTextView.text = quote.creator!!.username
                userphotoImageView.load(R.drawable.user)
                quoteTextView.text = quote.quote
                bookAuthor.text = quote.book!!.author
                bookImageView.load(API_URL + quote.book.image) {
                    crossfade(600)
                }
                likeCountTextView.text = quote.likes?.size.toString()
                bookNameTextView.text = quote.book.name
            }
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.username_textView, R.id.userphoto_imageView -> {
                    OnUserClickListener!!(quoteList[adapterPosition].creator!!.id)
                }
                R.id.viewBookBtn, R.id.book_info_container -> {
                    OnBookClickListener!!(quoteList[adapterPosition].book!!.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.full_post_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeAdapter.ViewHolder, position: Int) {
        Log.d("mytag", "onBindViewHolder: ")
        holder.bindData()
    }

    override fun getItemCount() = quoteList.size

    fun setData(newQuoteList: List<Quote>) {
        val callback = DiffUtilCallback(newQuoteList, quoteList)
        val diffResult = DiffUtil.calculateDiff(callback)
        quoteList = newQuoteList
        diffResult.dispatchUpdatesTo(this)
    }
}