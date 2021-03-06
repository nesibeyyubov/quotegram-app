package com.nesib.yourbooknotes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.SearchBookLayoutBinding
import com.nesib.yourbooknotes.models.Book
import com.nesib.yourbooknotes.utils.Constants.API_URL
import com.nesib.yourbooknotes.utils.DiffUtilCallback

class SearchBooksAdapter : RecyclerView.Adapter<SearchBooksAdapter.ViewHolder>() {
    private var bookList:List<Book> = emptyList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = SearchBookLayoutBinding.bind(itemView)
        fun bindData(book:Book){
            binding.apply {
                searchBookName.text = book.name
                searchBookAuthor.text = book.author
                searchBookGenre.text = book.genre
                searchBookImage.load(API_URL+ book.image){
                    crossfade(400)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchBooksAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.search_book_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchBooksAdapter.ViewHolder, position: Int) {
        holder.bindData(bookList[position])
    }

    fun setData(newBookList:List<Book>){
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallback(newBookList,bookList))
        bookList = newBookList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = bookList.size
}