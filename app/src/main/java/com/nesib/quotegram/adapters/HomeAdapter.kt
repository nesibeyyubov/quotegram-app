package com.nesib.quotegram.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.FullPostLayoutBinding
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.utils.Constants.API_URL
import com.nesib.quotegram.utils.toFormattedNumber

class HomeAdapter(val dialog: AlertDialog) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    var currentUserId: String? = null
    var OnUserClickListener: ((String) -> Unit)? = null
    var onQuoteOptionsClickListener: ((Quote) -> Unit)? = null
    var onLikeClickListener: ((Quote) -> Unit)? = null
    var onDownloadClickListener: ((Quote) -> Unit)? = null
    var onShareClickListener: ((Quote) -> Unit)? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var binding: FullPostLayoutBinding = FullPostLayoutBinding.bind(itemView)

        init {
            binding.usernameTextView.setOnClickListener(this)
            binding.userphotoImageView.setOnClickListener(this)
            binding.postOptionsBtn.setOnClickListener(this)
            binding.likeBtn.setOnClickListener(this)
            binding.downloadButton.setOnClickListener(this)
            binding.shareBtn.setOnClickListener(this)
        }

        fun bindData() {
            val quote = differ.currentList[adapterPosition]
            binding.apply {
                usernameTextView.text = quote.creator?.username
                if (quote.creator?.profileImage != "" && quote.creator?.profileImage != null) {
                    userphotoImageView.load(quote.creator?.profileImage)
                } else {
                    userphotoImageView.load(R.drawable.user)
                }

                quoteTextView.text = quote.quote

                likeCountTextView.text = quote.likes?.size?.toFormattedNumber()
                if (quote.liked) {
                    likeBtn.setImageResource(R.drawable.ic_like_blue)
                } else {
                    likeBtn.setImageResource(R.drawable.ic_like)
                }
                genreText.text = "#${quote.genre}"
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
                R.id.username_textView, R.id.userphoto_imageView -> {
                    OnUserClickListener?.let {
                        it((differ.currentList[adapterPosition].creator!!.id))
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
                R.id.shareBtn -> {
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
        Log.d("mytag", "setData: ${newQuoteList.size}")
        differ.submitList(newQuoteList)
    }
}