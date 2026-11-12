package com.example.bookstore_android.presentation.viewmodel

import com.example.bookstore_android.domain.model.User

/**
 * Estados inmutables de la interfaz de usuario (Patrón MVVM).
 */
sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
