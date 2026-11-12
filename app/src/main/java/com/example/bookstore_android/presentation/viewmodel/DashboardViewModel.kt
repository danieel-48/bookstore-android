package com.example.bookstore_android.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookstore_android.domain.repository.DashboardRepository
import com.example.bookstore_android.domain.repository.LoginRepository

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<DashboardUiState>(DashboardUiState.Loading)
    val uiState: LiveData<DashboardUiState> get() = _uiState

    var currentState: DashboardUiState = DashboardUiState.Loading
        private set

    init {
        loadStats()
    }

    fun loadStats() {
        updateState(DashboardUiState.Loading)
        dashboardRepository.getStats { result ->
            result.fold(
                onSuccess = { stats ->
                    updateState(DashboardUiState.Success(stats))
                },
                onFailure = { error ->
                    updateState(DashboardUiState.Error(error.message ?: "Error al cargar las métricas."))
                }
            )
        }
    }

    fun getLoggedUser() = loginRepository.getSavedUser()

    fun logout() {
        loginRepository.clearSession()
    }

    private fun updateState(state: DashboardUiState) {
        currentState = state
        try {
            _uiState.postValue(state)
        } catch (_: Exception) {}
    }
}

class DashboardViewModelFactory(
    private val dashboardRepository: DashboardRepository,
    private val loginRepository: LoginRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(dashboardRepository, loginRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
