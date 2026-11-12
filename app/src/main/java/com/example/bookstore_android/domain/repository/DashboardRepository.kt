package com.example.bookstore_android.domain.repository

import com.example.bookstore_android.domain.model.DashboardStats

interface DashboardRepository {
    fun getStats(callback: (Result<DashboardStats>) -> Unit)
}
