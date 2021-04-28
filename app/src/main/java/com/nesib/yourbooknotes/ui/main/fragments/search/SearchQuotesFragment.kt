package com.nesib.yourbooknotes.ui.main.fragments.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nesib.yourbooknotes.R
import com.nesib.yourbooknotes.adapters.HomeAdapter
import com.nesib.yourbooknotes.databinding.FragmentSearchQuotesBinding
import com.nesib.yourbooknotes.models.Quote
import com.nesib.yourbooknotes.ui.main.MainActivity
import com.nesib.yourbooknotes.ui.main.fragments.HomeFragmentDirections
import com.nesib.yourbooknotes.ui.viewmodels.AuthViewModel
import com.nesib.yourbooknotes.ui.viewmodels.QuoteViewModel
import com.nesib.yourbooknotes.ui.viewmodels.SharedViewModel
import com.nesib.yourbooknotes.utils.Constants.MIN_GENRE_COUNT
import com.nesib.yourbooknotes.utils.DataState
import com.nesib.yourbooknotes.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SearchQuotesFragment : Fragment(R.layout.fragment_search_quotes) {
    private lateinit var binding: FragmentSearchQuotesBinding
    private val homeAdapter by lazy { HomeAdapter((activity as MainActivity).dialog) }
    private val quoteViewModel: QuoteViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels({ requireActivity() })
    private val args by navArgs<SearchQuotesFragmentArgs>()

    private var currentPage = 1
    private var paginatingFinished = false
    private var paginationLoading = false
    private var currentQuotes: List<Quote> = emptyList()
    private var followingGenres: String = ""

    private var genres: MutableList<String> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchQuotesBinding.bind(view)
        setHasOptionsMenu(true)
        setupRecyclerView()
        subscribeObservers()
        setupClickListeners()
        quoteViewModel.getQuotesByGenre(args.genre)
    }

    private fun setupClickListeners(){
        binding.tryAgainButton.setOnClickListener {
            quoteViewModel.getQuotesByGenre(args.genre,forced = true)
        }
    }


    private fun subscribeObservers() {
        quoteViewModel.quotes.observe(viewLifecycleOwner) {
            when (it) {
                is DataState.Success -> {
                    if (binding.failContainer.visibility == View.VISIBLE) {
                        binding.failContainer.visibility = View.GONE
                    }
                    if (it.data?.quotes?.size == 0) {
                        binding.noQuoteFoundContainer.visibility = View.VISIBLE
                        binding.noQuoteGenreValue.text = "#${args.genre}"
                    }
                    if (paginationLoading) {
                        binding.paginationProgressBar.visibility = View.INVISIBLE
                        paginationLoading = false
                        if (currentQuotes.size == it.data!!.quotes.size) {
                            paginatingFinished = true
                        }
                    }
                    currentQuotes = it.data!!.quotes
                    binding.shimmerLayout.visibility = View.INVISIBLE
                    homeAdapter.setData(currentQuotes)
                }
                is DataState.Fail -> {
                    binding.failMessage.text =it.message
                    binding.failContainer.visibility = View.VISIBLE
                    binding.shimmerLayout.visibility = View.INVISIBLE
                    binding.shimmerLayout.stopShimmer()
                    showToast(it.message)
                }
                is DataState.Loading -> {
                    if (binding.failContainer.visibility == View.VISIBLE) {
                        binding.failContainer.visibility = View.GONE
                    }
                    if (paginationLoading) {
                        binding.paginationProgressBar.visibility = View.VISIBLE
                    } else {
                        binding.shimmerLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        homeAdapter.onDownloadClickListener = { quote ->
            val action = SearchQuotesFragmentDirections.actionGlobalDownloadQuoteFragment(
                quote.quote,
                quote.book?.name
            )
            findNavController().navigate(action)
        }
        homeAdapter.onShareClickListener = { quote ->
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, quote.quote + "\n\n#Quotegram App")
            val shareIntent = Intent.createChooser(intent,"Share Quote")
            startActivity(shareIntent)
        }
        homeAdapter.currentUserId = authViewModel.currentUserId
        homeAdapter.onQuoteOptionsClickListener = { quote ->
            val action = SearchQuotesFragmentDirections.actionGlobalQuoteOptionsFragment(quote)
            findNavController().navigate(action)
        }
        homeAdapter.onLikeClickListener = { quote ->
            quoteViewModel.toggleLike(quote)
        }
        homeAdapter.OnBookClickListener = { bookId ->
            val action = SearchQuotesFragmentDirections.actionSearchQuotesFragmentToBookProfileFragment(bookId)
            findNavController().navigate(action)
        }
        homeAdapter.OnUserClickListener = { userId ->
            if (userId != authViewModel.currentUserId) {
                val action = SearchQuotesFragmentDirections.actionSearchQuotesFragmentToUserProfileFragment(userId)
                findNavController().navigate(action)
            } else {
                findNavController().navigate(R.id.action_global_myProfileFragment)
            }
        }
        homeAdapter.currentUserId = authViewModel.currentUserId

        val mLayoutManager = LinearLayoutManager(requireContext())
        binding.searchQuotesRecyclerView.apply {
            adapter = homeAdapter
            layoutManager = mLayoutManager
            this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        if (!paginatingFinished && mLayoutManager.findLastCompletelyVisibleItemPosition() == (currentQuotes.size - 1) && !paginationLoading) {
                            currentPage++
                            paginationLoading = true
                            quoteViewModel.getQuotesByGenre(args.genre, currentPage)
                        }
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.discover_quotes_menu, menu)
        followingGenres = authViewModel.getFollowingGenres()
        genres = followingGenres.split(",").toMutableList()
        if (followingGenres.contains(args.genre.toLowerCase(Locale.ROOT))) {
            menu.findItem(R.id.follow_genre_menu_item).title = "Following"
        } else {
            menu.findItem(R.id.follow_genre_menu_item).title = "Follow"
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var genresChanged = true
        if (item.itemId == R.id.follow_genre_menu_item) {
            if (!genres.contains(args.genre)) {
                genres.add(args.genre)
                item.title = "Following"
            } else {
                if (genres.size > 3) {
                    genres.remove(args.genre)
                    item.title = "Follow"
                } else {
                    genresChanged = false
                    showToast("You should at least select ${MIN_GENRE_COUNT} genres")
                }
            }
            if (genresChanged) {
                val genresText = genres.toCustomizedString()
                authViewModel.saveFollowingGenres(genresText, authViewModel.currentUserId)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun MutableList<String>.toCustomizedString(): String {
        var text = ""
        this.forEachIndexed { index, genre ->
            text += if (index != this.size - 1) {
                "$genre,"
            } else {
                genre
            }
        }
        return text
    }
}