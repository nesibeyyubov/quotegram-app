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

    private val _searchTextGenre = MutableLiveData<String>()
    val searchTextGenre
        get() = _searchTextGenre


    fun setChangedText(text: String) {
        when(currentTabIndex){
            0 ->{
                _searchTextGenre.value = text
            }
            1 ->{
                _searchTextBook.value = text
            }
            2 ->{
                _searchTextUser.value = text
            }
        }

    }


}