package com.nesib.yourbooknotes.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.QuoteFromBookLayoutBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.models.User

class BookQuotesAdapter : RecyclerView.Adapter<BookQuotesAdapter.ViewHolder>() {
    var onUserClickListener: ((User?) -> Unit)? = null
    var onQuoteOptionsClicked:((Quote)->Unit)? = null
    var currentUserId:String? = null
    var onLikeClickListener:((Quote)->Unit)? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var binding:QuoteFromBookLayoutBinding = QuoteFromBookLayoutBinding.bind(itemView)

        init {
            binding.username.setOnClickListener(this)
            binding.userImage.setOnClickListener(this)
            binding.postOptionsBtn.setOnClickListener(this)
            binding.likeBtn.setOnClickListener(this)
        }
        fun bindData(){
            val quote = differ.currentList[adapterPosition]
            binding.apply {
                username.text = quote.creator?.username
                userImage.load(quote.creator?.profileImage){
                    error(R.drawable.user)
                }
                bookQuoteTextView.text = quote.quote
                quoteLikesCount.text = quote.likes?.size.toString()
                if(quote.liked){
                    likeBtn.setImageResource(R.drawable.ic_like_blue)
                }else{
                    likeBtn.setImageResource(R.drawable.ic_like)
                }
            }
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.likeBtn -> {
                    val quote = differ.currentList[adapterPosition]
                    val likes = quote.likes!!.toMutableList()
                    if (!quote.liked) {
                        binding.likeBtn.setImageResource(R.drawable.ic_like_blue)
                        likes.add(currentUserId!!)
                    } else {
                        binding.likeBtn.setImageResource(R.drawable.ic_like)
                        likes.remove(currentUserId)
                    }
                    quote.liked = !quote.liked
                    quote.likes = likes.toList()
                    binding.quoteLikesCount.text = quote.likes!!.size.toString()
                    onLikeClickListener!!(quote)
                }
                R.id.username,R.id.userImage -> {
                    onUserClickListener!!(differ.currentList[adapterPosition].creator)
                }
                R.id.postOptionsBtn->{
                    onQuoteOptionsClicked!!(differ.currentList[adapterPosition])
                }
            }
        }
    }

    private val diffCallback = object: DiffUtil.ItemCallback<Quote>(){
        override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(this,diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookQuotesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quote_from_book_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookQuotesAdapter.ViewHolder, position: Int) {
        holder.bindData()
    }

    override fun getItemCount() = differ.currentList.size

    fun setData(newQuoteList:List<Quote>){
        differ.submitList(newQuoteList)
    }
}