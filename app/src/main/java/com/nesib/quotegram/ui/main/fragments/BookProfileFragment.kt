package com.nesib.quotegram.ui.main.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.nesib.quotegram.R
import com.nesib.quotegram.adapters.BookQuotesAdapter
import com.nesib.quotegram.databinding.FragmentBookProfileBinding
import com.nesib.quotegram.databinding.ReportDialogBinding
import com.nesib.quotegram.models.Book
import com.nesib.quotegram.models.Quote
import com.nesib.quotegram.ui.main.MainActivity
import com.nesib.quotegram.ui.viewmodels.AuthViewModel
import com.nesib.quotegram.ui.viewmodels.BookViewModel
import com.nesib.quotegram.ui.viewmodels.QuoteViewModel
import com.nesib.quotegram.ui.viewmodels.ReportViewModel
import com.nesib.quotegram.utils.Constants.API_URL
import com.nesib.quotegram.utils.Constants.TEXT_DELETED_QUOTE
import com.nesib.quotegram.utils.Constants.TEXT_NEW_QUOTE
import com.nesib.quotegram.utils.Constants.TEXT_UPDATED_QUOTE
import com.nesib.quotegram.utils.DataState
import com.nesib.quotegram.utils.showToast
import com.nesib.quotegram.utils.toFormattedNumber
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookProfileFragment : Fragment(R.layout.fragment_book_profile) {
    private val bookViewModel: BookViewModel by viewModels()
    private val quoteViewModel: QuoteViewModel by viewModels({ requireActivity() })
    private val authViewModel: AuthViewModel by viewModels()
    private val reportViewModel: ReportViewModel by viewModels()
    private val args by navArgs<BookProfileFragmentArgs>()

    private lateinit var binding: FragmentBookProfileBinding
    private var currentBook: Book? = null
    private var currentBookQuotes: MutableList<Quote>? = null
    private var paginationLoading = false
    private var currentPage = 1
    private var paginatingFinished = false

    private var makeSureDialog: AlertDialog? = null
    private val notAuthDialog by lazy { (activity as MainActivity).dialog }
    private val bookQuotesAdapter by lazy { BookQuotesAdapter(notAuthDialog) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookProfileBinding.bind(view)
        bookViewModel.getBook(args.bookId)
        setupClickListeners()
        setHasOptionsMenu(true)
        setupRecyclerView()
        subscribeObservers()
        setFragmentResultListener()

    }

    private fun setFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            TEXT_NEW_QUOTE,
            viewLifecycleOwner
        ) { requestKey: String, newQuote: Bundle ->
            currentBookQuotes?.add(0, newQuote[TEXT_NEW_QUOTE] as Quote)
            val newList = currentBookQuotes?.toList()
            newList?.let { bookQuotesAdapter.setData(it) }
        }

        parentFragmentManager.setFragmentResultListener(
            TEXT_DELETED_QUOTE,
            viewLifecycleOwner
        ) { requestKey: String, deletedQuote: Bundle ->
            bookViewModel.notifyQuoteRemoved(deletedQuote[TEXT_DELETED_QUOTE] as Quote)
        }

        parentFragmentManager.setFragmentResultListener(
            TEXT_UPDATED_QUOTE,
            viewLifecycleOwner
        ) { s: String, updatedQuote: Bundle ->
            bookViewModel.notifyQuoteUpdated((updatedQuote[TEXT_UPDATED_QUOTE] as Quote))
        }
    }

    private fun subscribeObservers() {
        bookViewModel.book.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if (binding.failContainer.visibility == View.VISIBLE) {
                        binding.failContainer.visibility = View.GONE
                    }
                    binding.bookProfileContent.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    currentBook = it.data?.book!!
                    currentBookQuotes = currentBook?.quotes?.toMutableList()

                    bindData(it.data.book)
                }
                is DataState.Fail -> {
                    binding.failContainer.visibility = View.VISIBLE
                    binding.failMessage.text = it.message
                    showToast(it.message!!)
                    binding.progressBar.visibility = View.GONE
                }
                is DataState.Loading -> {
                    if (binding.failContainer.visibility == View.VISIBLE) {
                        binding.failContainer.visibility = View.GONE
                    }
                    binding.bookProfileContent.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        bookViewModel.quotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if (currentBookQuotes?.size == it.data!!.quotes.size) {
                        paginatingFinished = true
                    }
                    binding.paginationProgressBar.visibility = View.GONE
                    paginationLoading = false
                    bookQuotesAdapter.setData(it.data.quotes)
                    currentBookQuotes = it.data.quotes.toMutableList()
                }
                is DataState.Fail -> {
                    Toast.makeText(requireContext(), "Failed....", Toast.LENGTH_SHORT).show()
                    paginationLoading = false
                }
                is DataState.Loading -> {
                    paginationLoading = true
                    binding.paginationProgressBar.visibility = View.VISIBLE
                }
            }
        }

        reportViewModel.report.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    showToast(it.data!!.message)
                    makeSureDialog?.dismiss()
                }
                is DataState.Fail -> {
                    showToast(it.message)
                    makeSureDialog?.dismiss()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        bookQuotesAdapter.currentUserId = authViewModel.currentUserId
        bookQuotesAdapter.onLikeClickListener = { quote ->
            quoteViewModel.toggleLike(quote)
        }
        bookQuotesAdapter.onQuoteOptionsClicked = { quote ->
            quote.book = Book(
                id = currentBook!!.id,
                name = currentBook!!.name,
                author = currentBook!!.author,
                image = currentBook!!.image,
            )
            val action = BookProfileFragmentDirections.actionGlobalQuoteOptionsFragment(quote)
            findNavController().navigate(action)
        }
        bookQuotesAdapter.onUserClickListener = { user ->
            user?.let {
                val action =
                    BookProfileFragmentDirections.actionBookProfileFragmentToUserProfileFragment(it.id)
                findNavController().navigate(action)
            }
        }
        bookQuotesAdapter.onDownloadClickListener = { quote ->
            val action = BookProfileFragmentDirections.actionGlobalDownloadQuoteFragment(
                quote.quote,
                currentBook?.name
            )
            findNavController().navigate(action)
        }
        bookQuotesAdapter.onShareClickListener = { quote ->
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, quote.quote + "\n\n#Quotegram App")
            val shareIntent = Intent.createChooser(intent, "Share Quote")
            startActivity(shareIntent)

        }

        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.bookQuotesRecyclerView.apply {
            adapter = bookQuotesAdapter
            layoutManager = mLayoutManager
        }
        binding.bookProfileContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                val notReachedBottom = v.canScrollVertically(1)
                if (!notReachedBottom && !paginationLoading && !paginatingFinished) {
                    currentPage++
                    bookViewModel.getMoreBookQuotes(currentBook!!.id, currentPage)
                }
            }
        })

    }

    private fun bindData(book: Book) {
        binding.apply {
            bookImage.load(API_URL + book.image) {
                crossfade(400)
            }
            bookName.text = book.name
            bookAuthor.text = book.author
            bookGenre.text = book.genre
            bookQuoteCount.text = (book.totalQuoteCount ?: 0).toFormattedNumber()
            bookFollowerCount.text = (book.followers?.size ?: 0).toFormattedNumber()
            toggleFollowButtonStyle(book.following)
            if (book.quotes!!.isEmpty()) {
                noQuoteFoundContainer.visibility = View.VISIBLE
            }
        }
        bookQuotesAdapter.setData(currentBookQuotes!!.toList())
    }

    private fun toggleFollowButtonStyle(following: Boolean) {
        binding.apply {
            bookFollowButton.setBackgroundResource(if (following) R.drawable.add_quote_from_this_book_bg else R.drawable.follow_button_bg)
            bookFollowButtonText.text = if (following) "Following" else "Follow"
            bookFollowButtonText.setTextColor(
                if (following) ContextCompat.getColor(
                    requireContext(),
                    R.color.blue
                ) else ContextCompat.getColor(requireContext(), R.color.white)
            )

        }
    }

    private fun setupClickListeners() {
        binding.addBookFromThisBookBtn.setOnClickListener {
            val action = BookProfileFragmentDirections.actionBookProfileFragmentToAddQuoteFragment()
            action.book = currentBook
            findNavController().navigate(action)
        }
        binding.tryAgainButton.setOnClickListener {
            bookViewModel.getBook(args.bookId, forced = true)
        }

        binding.bookFollowButton.setOnClickListener {
            if (authViewModel.currentUserId != null) {
                currentBook?.let { book ->
                    val followers = book.followers!!.toMutableList()
                    if (!book.following) {
                        followers.add(authViewModel.currentUserId!!)
                        toggleFollowButtonStyle(true)
                        binding.bookFollowerCount.text = (binding.bookFollowerCount.text.toString()
                            .toInt() + 1).toString()
                    } else {
                        followers.remove(authViewModel.currentUserId!!)
                        toggleFollowButtonStyle(false)
                        binding.bookFollowerCount.text = (binding.bookFollowerCount.text.toString()
                            .toInt() - 1).toString()
                    }
                    book.followers = followers.toList()
                    book.following = !book.following
                    bookViewModel.toggleBookFollow(book)
                }
            } else {
                notAuthDialog.show()
            }

        }
    }

    private fun showMakeSureDialogForReport() {
        val view = layoutInflater.inflate(R.layout.report_dialog, binding.root, false)
        val binding = ReportDialogBinding.bind(view)
        makeSureDialog = AlertDialog.Builder(requireContext()).setView(binding.root).create()
        makeSureDialog!!.show()

        binding.apply {
            notNowButton.setOnClickListener { makeSureDialog!!.dismiss() }
            reportButton.setOnClickListener {
                makeSureDialog!!.setCancelable(false)
                binding.reportProgressBar.visibility = View.VISIBLE
                binding.reportButton.visibility = View.INVISIBLE
                binding.notNowButton.isEnabled = false
                reportViewModel.reportBook(authViewModel.currentUserId ?: "", args.bookId)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.book_profile_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.report_book_menu_item) {
            showMakeSureDialogForReport()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}