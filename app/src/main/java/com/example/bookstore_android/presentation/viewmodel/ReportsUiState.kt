package com.example.bookstore_android.presentation.viewmodel

import com.example.bookstore_android.domain.model.BookItem
import com.example.bookstore_android.domain.model.ClientItem
import com.example.bookstore_android.domain.model.ReportFilters
import com.example.bookstore_android.domain.model.ReportResult

sealed class ReportsUiState {
    object Loading : ReportsUiState()
    data class Success(
        val result: ReportResult,
        val clients: List<ClientItem>,
        val books: List<BookItem>,
        val currentFilters: ReportFilters
    ) : ReportsUiState()
    data class Error(val message: String) : ReportsUiState()
}
