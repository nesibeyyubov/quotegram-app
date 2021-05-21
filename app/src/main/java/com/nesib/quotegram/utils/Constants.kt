package com.nesib.quotegram.utils

object Constants {
    // Server codes
    const val CODE_SUCCESS = 200
    const val CODE_CREATION_SUCCESS = 201
    const val CODE_VALIDATION_FAIL = 422
    const val CODE_AUTHENTICATION_FAIL = 401
    const val CODE_SERVER_ERROR = 500
    const val API_URL = "https://book-quotes-app1.herokuapp.com/"

    // Quantities
    const val MIN_GENRE_COUNT = 3
    const val MAX_QUOTE_LENGTH = 500
    const val MIN_QUOTE_LENGTH = 15

    // Strings
    const val KEY_THEME = "theme"
    const val DEFAULT_SELECTED_GENRE = "all"
    const val TEXT_NEW_QUOTE="newQuote"
    const val TEXT_DELETED_QUOTE="deletedQuote"
    const val TEXT_UPDATED_QUOTE="updatedQuote"
    const val TEXT_DIRECT_TO_LOGIN="directToLogin"
    const val TEXT_DIRECT_TO_SIGNUP="directToSignup"
    const val TEXT_UPDATED_USER="updatedUser"

    const val CONTACT_EMAIL="app.quotegram@gmail.com"

    const val PRIVACY_POLICY_URL = "https://quotegram.netlify.app/"


}