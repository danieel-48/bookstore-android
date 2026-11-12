package com.example.bookstore_android.presentation.viewmodel

import com.example.bookstore_android.domain.model.DashboardStats

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val stats: DashboardStats) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
