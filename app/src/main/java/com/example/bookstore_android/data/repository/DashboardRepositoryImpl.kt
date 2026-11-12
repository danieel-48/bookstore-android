package com.example.bookstore_android.data.repository

import com.example.bookstore_android.data.remote.DashboardApi
import com.example.bookstore_android.domain.model.DashboardStats
import com.example.bookstore_android.domain.repository.DashboardRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class DashboardRepositoryImpl(
    private val dashboardApi: DashboardApi,
    private val backgroundExecutor: Executor = Executors.newSingleThreadExecutor()
) : DashboardRepository {

    override fun getStats(callback: (Result<DashboardStats>) -> Unit) {
        backgroundExecutor.execute {
            try {
                val dto = dashboardApi.fetchStats()
                val domainStats = DashboardStats(
                    totalBooks = dto.totalBooks,
                    totalClients = dto.totalClients,
                    salesToday = dto.salesToday,
                    transactionsToday = dto.transactionsToday
                )
                callback(Result.success(domainStats))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }
}
