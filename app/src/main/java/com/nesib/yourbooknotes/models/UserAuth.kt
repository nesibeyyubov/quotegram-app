package com.nesib.yourbooknotes.models

data class UserAuth(
    val username:String?,
    val email:String?,
    val profileImage:String?,
    val userId:String?,
    val token:String?,
    val followingGenres:String?
)
