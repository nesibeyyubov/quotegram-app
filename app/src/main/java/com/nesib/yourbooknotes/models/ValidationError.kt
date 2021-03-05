package com.nesib.yourbooknotes.models

data class ValidationError(
    val msg:String,
    val value:String,
    val param:String,
)
