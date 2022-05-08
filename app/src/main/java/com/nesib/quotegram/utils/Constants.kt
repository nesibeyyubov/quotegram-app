package com.nesib.quotegram.utils

import com.nesib.quotegram.R

object Constants {
    // Server codes
    const val CODE_SUCCESS = 200
    const val CODE_CREATION_SUCCESS = 201
    const val CODE_VALIDATION_FAIL = 422
    const val CODE_AUTHENTICATION_FAIL = 401
    const val CODE_SERVER_ERROR = 500
    const val API_URL = "http://164.92.148.254:4000/"

    const val ADMIN_ID="60670d9b34d95b1a30dc8c0d"

    // Quantities
    const val MIN_GENRE_COUNT = 1
    const val MAX_QUOTE_LENGTH = 500
    const val MIN_QUOTE_LENGTH = 15
    const val MIN_PASSWORD_LENGTH = 5
    const val MIN_USERNAME_LENGTH = 5

    // Strings
    const val KEY_THEME = "theme"
    const val DEFAULT_SELECTED_GENRE = "all"
    const val KEY_NEW_QUOTE = "newQuote"
    const val KEY_DELETED_QUOTE = "deletedQuote"
    const val KEY_UPDATED_QUOTE = "updatedQuote"
    const val TEXT_DIRECT_TO_LOGIN = "directToLogin"
    const val TEXT_DIRECT_TO_SIGNUP = "directToSignup"
    const val TEXT_UPDATED_USER = "updatedUser"

    // Api Keys
    const val KEY_QUOTE = "quote"
    const val KEY_GENRE = "genre"
    const val KEY_PASSWORD = "password"
    const val KEY_USERNAME = "username"
    const val KEY_FULL_NAME = "fullname"
    const val KEY_PROFILE_IMAGE = "profileImage"
    const val KEY_EMAIL = "email"


    const val CONTACT_EMAIL = "app.quotegram@gmail.com"

    const val PRIVACY_POLICY_URL = "https://quotegram.netlify.app/"

    val ROOT_SCREENS = listOf(
        R.id.homeFragment,
        R.id.myProfileFragment,
        R.id.notificationsFragment,
        R.id.searchFragment
    )

}