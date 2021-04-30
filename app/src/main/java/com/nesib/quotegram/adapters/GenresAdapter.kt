package com.nesib.quotegram.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nesib.quotegram.R
import com.nesib.quotegram.databinding.GenreItemLayoutBinding
import java.util.*

class GenresAdapter(val followingGenres:List<String>) : RecyclerView.Adapter<GenresAdapter.ViewHolder>() {
    var onGenreClickListener:((String)->Unit)? = null
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = GenreItemLayoutBinding.bind(itemView)
        init {
            binding.root.setOnClickListener {
                onGenreClickListener?.let { it(differ.currentList[adapterPosition]) }
            }
        }
        fun bindData(genre: String) {
            binding.genreName.text = "#$genre"
            binding.followingGenre.text = if(followingGenres.contains(genre.toLowerCase(Locale.ROOT))) "Following" else "Not Following"
        }
    }

    val diffItemCallback = object: DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    fun setData(newList:List<String>){
        differ.submitList(newList)
    }

    val differ = AsyncListDiffer(this,diffItemCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenresAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.genre_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenresAdapter.ViewHolder, position: Int) {
        holder.bindData(differ.currentList[position])
    }

    override fun getItemCount() = differ.currentList.size
}