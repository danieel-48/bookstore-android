package com.example.bookstore_android.data.remote

data class DashboardStatsDto(
    val totalBooks: Int,
    val totalClients: Int,
    val salesToday: Double,
    val transactionsToday: Int
)

interface DashboardApi {
    @Throws(Exception::class)
    fun fetchStats(): DashboardStatsDto
}
