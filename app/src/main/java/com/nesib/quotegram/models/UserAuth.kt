package com.nesib.quotegram.models

data class UserAuth(
    val message:String?=null,
    val email:String?,
    val userId:String?,
    val token:String?,
    val profileImage:String?,
    val username:String?,
    val followingGenres:List<String>?
)
