package com.example.bookstore_android.domain.model

data class User(
    val id: Long,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean
)
