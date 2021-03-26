package com.nesib.yourbooknotes.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var currentTabIndex = 0
    var toolbarText = ""

    private val _searchTextBook = MutableLiveData<String>()
    val searchTextBook
        get() = _searchTextBook

    private val _searchTextUser = MutableLiveData<String>()
    val searchTextUser
        get() = _searchTextUser


    fun setChangedText(text: String) {
         if (currentTabIndex == 1) {
            _searchTextBook.value = text
        } else {
            _searchTextUser.value = text
        }
    }



}