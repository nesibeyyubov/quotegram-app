package com.nesib.quotegram.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nesib.quotegram.ui.main.fragments.search.SearchTab

class SharedViewModel : ViewModel() {
    var currentTab: SearchTab = SearchTab.Quotes
    var toolbarText = ""

    private val _searchTextUser = MutableLiveData<String>()
    val searchTextUser
        get() = _searchTextUser

    private val _searchTextGenre = MutableLiveData<String>()
    val searchTextGenre
        get() = _searchTextGenre

    fun setChangedText(text: String) {
        when (currentTab) {
            SearchTab.Quotes -> {
                _searchTextGenre.value = text
            }
            SearchTab.Users -> {
                _searchTextUser.value = text
            }
        }

    }

}

