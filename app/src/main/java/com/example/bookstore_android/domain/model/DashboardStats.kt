package com.example.bookstore_android.domain.model

data class DashboardStats(
    val totalBooks: Int,
    val totalClients: Int,
    val salesToday: Double,
    val transactionsToday: Int
)
