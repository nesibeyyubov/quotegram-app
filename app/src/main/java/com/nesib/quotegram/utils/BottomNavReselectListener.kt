package com.nesib.quotegram.utils

interface BottomNavReselectListener {
    fun itemReselected(screen: Screen?)
}

enum class Screen { Home, Search, Notifications, MyProfile }