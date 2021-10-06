package com.nesib.quotegram.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.FullPostLayoutBinding
import com.nesib.quotegram.databinding.FullScreenQuoteItemBinding
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.utils.Constants.API_URL
import com.nesib.quotegram.utils.toFormattedNumber

class FullPageQuoteAdapter(val dialog: AlertDialog) :
    RecyclerView.Adapter<FullPageQuoteAdapter.ViewHolder>() {
    var currentUserId: String? = null
    var OnUserClickListener: ((String) -> Unit)? = null
    var OnBookClickListener: ((String) -> Unit)? = null
    var onQuoteOptionsClickListener: ((Quote) -> Unit)? = null
    var onLikeClickListener: ((Quote) -> Unit)? = null
    var onDownloadClickListener: ((Quote) -> Unit)? = null
    var onShareClickListener: ((Quote) -> Unit)? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var binding: FullScreenQuoteItemBinding = FullScreenQuoteItemBinding.bind(itemView)

        init {
            binding.userPhotoImageView.setOnClickListener(this)
            binding.bookInfoContainer.setOnClickListener(this)
            binding.postOptionsBtn.setOnClickListener(this)
            binding.likeBtn.setOnClickListener(this)
            binding.downloadButton.setOnClickListener(this)
            binding.shareBtn.setOnClickListener(this)
        }

        fun bindData() {
            val quote = differ.currentList[adapterPosition]
            binding.apply {
                if (quote.creator?.profileImage != "" && quote.creator?.profileImage != null) {
                    userPhotoImageView.load(quote.creator?.profileImage)
                } else {
                    userPhotoImageView.load(R.drawable.user)
                }
                quoteTextView.text = quote.quote

                bookAuthor.text = quote.book?.author
                bookImageView.load(API_URL + quote.book?.image) {
                    crossfade(600)
                }
                likeCountTextView.text = quote.likes?.size?.toFormattedNumber()
                bookNameTextView.text = quote.book?.name
                if (quote.liked) {
                    likeBtn.setImageResource(R.drawable.ic_like_blue)
                } else {
                    likeBtn.setImageResource(R.drawable.ic_like)
                }
                val quoteCount = quote.book?.totalQuoteCount
                bookTotalQuoteCount.text = (quoteCount ?: 0).toString()
            }
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.likeBtn -> {
                    if (currentUserId != null) {
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
                        if (quote.liked) {
                            binding.likeBtn.startAnimation(
                                AnimationUtils.loadAnimation(
                                    binding.likeBtn.context,
                                    R.anim.bouncing_anim
                                )
                            )
                        }
                    } else {
                        dialog.show()
                    }


                }
                R.id.username_textView, R.id.userPhotoImageView -> {
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
                R.id.downloadButton -> {
                    onDownloadClickListener?.let {
                        it(differ.currentList[adapterPosition])
                    }
                }
                R.id.shareBtn->{
                    onShareClickListener?.let {
                        it(differ.currentList[adapterPosition])
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullPageQuoteAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.full_screen_quote_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FullPageQuoteAdapter.ViewHolder, position: Int) {
        holder.bindData()
    }

    override fun getItemCount() = differ.currentList.size

    fun setData(newQuoteList: List<Quote>) {
        differ.submitList(newQuoteList)
    }
}