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
import com.nesib.yourbooknotes.databinding.QuoteFromBookLayoutBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.utils.DiffUtilCallback

class BookQuotesAdapter : RecyclerView.Adapter<BookQuotesAdapter.ViewHolder>() {
    var quoteList = emptyList<Quote>()

    var OnUsernameTextViewClickListener: (() -> Unit)? = null
    var OnUserImageViewClickListener: (() -> Unit)? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var binding:QuoteFromBookLayoutBinding = QuoteFromBookLayoutBinding.bind(itemView)

        init {
            binding.userImage.setOnClickListener(this)
            binding.userImage.setOnClickListener(this)
        }
        fun bindData(){
            val quote = quoteList[adapterPosition]
            binding.apply {
                username.text = quote.creator!!.username
                userImage.load(R.drawable.user){
                    crossfade(600)
                }
                bookQuoteTextView.text = quote.quote
                quoteLikesCount.text = quote.likes?.size.toString()
            }
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.username -> {
                    OnUsernameTextViewClickListener!!()
                }
                R.id.userImage -> {
                    OnUserImageViewClickListener!!()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookQuotesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quote_from_book_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookQuotesAdapter.ViewHolder, position: Int) {
        holder.bindData()
    }

    override fun getItemCount() = quoteList.size

    fun setData(newQuoteList:List<Quote>){
        val callback = DiffUtilCallback(newQuoteList,quoteList)
        val diffResult = DiffUtil.calculateDiff(callback)
        quoteList = newQuoteList
        diffResult.dispatchUpdatesTo(this)
    }
}