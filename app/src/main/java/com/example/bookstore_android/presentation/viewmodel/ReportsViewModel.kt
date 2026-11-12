package com.example.bookstore_android.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookstore_android.domain.model.BookItem
import com.example.bookstore_android.domain.model.ClientItem
import com.example.bookstore_android.domain.model.ReportFilters
import com.example.bookstore_android.domain.model.ReportPeriod
import com.example.bookstore_android.domain.repository.ReportsRepository

class ReportsViewModel(
    private val repository: ReportsRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<ReportsUiState>(ReportsUiState.Loading)
    val uiState: LiveData<ReportsUiState> get() = _uiState

    var currentState: ReportsUiState = ReportsUiState.Loading
        private set

    var currentFilters: ReportFilters = ReportFilters(period = ReportPeriod.DAILY)
        private set

    private var availableClients: List<ClientItem> = emptyList()
    private var availableBooks: List<BookItem> = emptyList()

    init {
        loadData()
    }

    fun loadData() {
        updateState(ReportsUiState.Loading)
        repository.getFilterOptions { optionsResult ->
            optionsResult.fold(
                onSuccess = { (clients, books) ->
                    availableClients = clients
                    availableBooks = books
                    applyFilters(currentFilters)
                },
                onFailure = {
                    applyFilters(currentFilters)
                }
            )
        }
    }

    fun setPeriod(period: ReportPeriod) {
        currentFilters = currentFilters.copy(period = period)
        applyFilters(currentFilters)
    }

    fun setClientFilter(clientId: Long?) {
        currentFilters = currentFilters.copy(idClient = clientId)
        applyFilters(currentFilters)
    }

    fun setBookFilter(bookId: Long?) {
        currentFilters = currentFilters.copy(idBook = bookId)
        applyFilters(currentFilters)
    }

    fun setDateFilter(date: String?) {
        currentFilters = currentFilters.copy(date = date)
        applyFilters(currentFilters)
    }

    fun clearFilters() {
        currentFilters = ReportFilters(period = currentFilters.period)
        applyFilters(currentFilters)
    }

    private fun applyFilters(filters: ReportFilters) {
        updateState(ReportsUiState.Loading)
        repository.getReports(filters) { result ->
            result.fold(
                onSuccess = { reportResult ->
                    updateState(
                        ReportsUiState.Success(
                            result = reportResult,
                            clients = availableClients,
                            books = availableBooks,
                            currentFilters = filters
                        )
                    )
                },
                onFailure = { error ->
                    updateState(ReportsUiState.Error(error.message ?: "No se pudieron cargar los reportes."))
                }
            )
        }
    }

    private fun updateState(state: ReportsUiState) {
        currentState = state
        try {
            _uiState.postValue(state)
        } catch (_: Exception) {}
    }
}

class ReportsViewModelFactory(
    private val repository: ReportsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            return ReportsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
