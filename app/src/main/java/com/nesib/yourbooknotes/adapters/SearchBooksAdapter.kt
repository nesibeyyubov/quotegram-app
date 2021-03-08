package com.nesib.yourbooknotes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.databinding.SearchBookLayoutBinding
import com.nesib.yourbooknotes.databinding.SelectBookLayoutBinding
import com.nesib.yourbooknotes.models.Book
import com.nesib.yourbooknotes.utils.Constants.API_URL

class SearchBooksAdapter(val isSelectBookFragment: Boolean = false) :
    RecyclerView.Adapter<SearchBooksAdapter.ViewHolder>() {
    private var bookList = emptyList<Book>()
    var onBookClickListener:((Book)->Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val searchLayoutBinding = if(!isSelectBookFragment) SearchBookLayoutBinding.bind(itemView) else null
        private val selectBookLayoutBinding = if(isSelectBookFragment) SelectBookLayoutBinding.bind(itemView) else null

        fun bindData(book: Book) {
            if(isSelectBookFragment){
                selectBookLayoutBinding?.apply {
                    searchBookName.text = book.name
                    searchBookAuthor.text = book.author
                    searchBookGenre.text = book.genre
                    searchBookImage.load(API_URL + book.image) {
                        crossfade(400)
                    }
                    selectBookBtn.setOnClickListener {
                        onBookClickListener!!(bookList[adapterPosition])
                    }
                }
            }
            else{
                searchLayoutBinding?.apply {
                    root.setOnClickListener {
                        onBookClickListener!!(book)
                    }
                    searchBookName.text = book.name
                    searchBookAuthor.text = book.author
                    searchBookGenre.text = book.genre
                    searchBookImage.load(API_URL + book.image) {
                        crossfade(400)
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchBooksAdapter.ViewHolder {
        val view = if (isSelectBookFragment)
            LayoutInflater.from(parent.context).inflate(R.layout.select_book_layout, parent, false)
        else
            LayoutInflater.from(parent.context).inflate(R.layout.search_book_layout, parent, false)


        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchBooksAdapter.ViewHolder, position: Int) {
        holder.bindData(bookList[position])
    }

    fun setData(newBookList: List<Book>) {
        bookList = newBookList
        notifyDataSetChanged()
    }

    override fun getItemCount() = bookList.size

}