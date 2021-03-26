package com.nesib.yourbooknotes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.GenreItemLayoutBinding
import java.util.*

class GenresAdapter(val genres: List<String>,val followingGenres:List<String>) : RecyclerView.Adapter<GenresAdapter.ViewHolder>() {
    var onGenreClickListener:((String)->Unit)? = null
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = GenreItemLayoutBinding.bind(itemView)
        init {
            binding.root.setOnClickListener {
                onGenreClickListener?.let { it(genres[adapterPosition]) }
            }
        }
        fun bindData(genre: String) {
            binding.genreName.text = "#$genre"
            binding.followingGenre.text = if(followingGenres.contains(genre.toLowerCase(Locale.ROOT))) "Following" else "Not Following"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenresAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.genre_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenresAdapter.ViewHolder, position: Int) {
        holder.bindData(genres[position])
    }

    override fun getItemCount() = genres.size
}