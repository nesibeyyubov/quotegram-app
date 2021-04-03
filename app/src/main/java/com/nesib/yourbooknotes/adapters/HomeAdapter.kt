package com.nesib.yourbooknotes.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.FullPostLayoutBinding
import com.nesib.yourbooknotes.databinding.NotAuthenticatedLayoutBinding
import com.nesib.yourbooknotes.models.Book
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.on_boarding.StartActivity
import com.nesib.yourbooknotes.utils.Constants.API_URL
import dagger.hilt.android.qualifiers.ApplicationContext

class HomeAdapter(val dialog:AlertDialog) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    var currentUserId: String? = null
    var OnUserClickListener: ((String) -> Unit)? = null
    var OnBookClickListener: ((String) -> Unit)? = null
    var onQuoteOptionsClickListener: ((Quote) -> Unit)? = null
    var onLikeClickListener: ((Quote) -> Unit)? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var binding: FullPostLayoutBinding = FullPostLayoutBinding.bind(itemView)

        init {
            binding.usernameTextView.setOnClickListener(this)
            binding.userphotoImageView.setOnClickListener(this)
            binding.viewBookBtn.setOnClickListener(this)
            binding.bookInfoContainer.setOnClickListener(this)
            binding.postOptionsBtn.setOnClickListener(this)
            binding.likeBtn.setOnClickListener(this)
        }

        fun bindData() {
            val quote = differ.currentList[adapterPosition]
            binding.apply {
                usernameTextView.text = quote.creator!!.username
                userphotoImageView.load(R.drawable.user)
                quoteTextView.text = quote.quote
                bookAuthor.text = quote.book?.author
                bookImageView.load(API_URL + quote.book?.image) {
                    crossfade(600)
                }
                likeCountTextView.text = quote.likes?.size.toString()
                bookNameTextView.text = quote.book?.name
                if (quote.liked) {
                    likeBtn.setImageResource(R.drawable.ic_like_blue)
                } else {
                    likeBtn.setImageResource(R.drawable.ic_like)
                }



            }
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.likeBtn -> {
                    if(currentUserId != null){
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
                        binding.likeCountTextView.text = quote.likes!!.size.toString()
                        onLikeClickListener?.let {
                            it(quote)
                        }
                        if(quote.liked){
                            binding.likeBtn.startAnimation(AnimationUtils.loadAnimation(binding.likeBtn.context,R.anim.bouncing_anim))
                        }
                    }else{
                        dialog.show()
                    }


                }
                R.id.username_textView, R.id.userphoto_imageView -> {
                    OnUserClickListener?.let {
                        it((differ.currentList[adapterPosition].creator!!.id))
                    }
                }
                R.id.viewBookBtn, R.id.book_info_container -> {
                    OnBookClickListener?.let {
                        it(differ.currentList[adapterPosition].book!!.id)
                    }
                }
                R.id.postOptionsBtn -> {
                    onQuoteOptionsClickListener?.let {
                        it((differ.currentList[adapterPosition]))
                    }
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Quote>() {
        override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.full_post_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeAdapter.ViewHolder, position: Int) {
        holder.bindData()
    }

    override fun getItemCount() = differ.currentList.size

    fun setData(newQuoteList: List<Quote>) {
        differ.submitList(newQuoteList)
    }
}