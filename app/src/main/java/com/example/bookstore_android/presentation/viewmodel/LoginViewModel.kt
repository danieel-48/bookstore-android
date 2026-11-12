package com.example.bookstore_android.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bookstore_android.domain.model.LoginCredentials
import com.example.bookstore_android.domain.model.User
import com.example.bookstore_android.domain.repository.LoginRepository
import com.example.bookstore_android.domain.validator.LoginValidator
import com.example.bookstore_android.domain.validator.ValidationResult

/**
 * Patrón MVVM - ViewModel:
 * Contiene la lógica de presentación del flujo de autenticación,
 * sin referencias a vistas de Android para garantizar testabilidad.
 */
class LoginViewModel(
    private val repository: LoginRepository,
    private val validator: LoginValidator = LoginValidator()
) : ViewModel() {

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Initial)
    val uiState: LiveData<LoginUiState> get() = _uiState

    // Estado observable para sincronización directa en pruebas unitarias
    var currentState: LoginUiState = LoginUiState.Initial
        private set

    init {
        checkExistingSession()
    }

    fun checkExistingSession() {
        val savedUser = repository.getSavedUser()
        if (savedUser != null) {
            updateState(LoginUiState.Success(savedUser))
        }
    }

    fun login(email: String, password: String) {
        val credentials = LoginCredentials(email, password)
        val validation = validator.validate(credentials)

        if (validation is ValidationResult.Invalid) {
            updateState(LoginUiState.Error(validation.message))
            return
        }

        updateState(LoginUiState.Loading)

        repository.login(credentials) { result ->
            result.fold(
                onSuccess = { user ->
                    updateState(LoginUiState.Success(user))
                },
                onFailure = { throwable ->
                    val message = throwable.message ?: "Error al iniciar sesión."
                    updateState(LoginUiState.Error(message))
                }
            )
        }
    }

    fun logout() {
        repository.clearSession()
        updateState(LoginUiState.Initial)
    }

    private fun updateState(newState: LoginUiState) {
        currentState = newState
        try {
            _uiState.postValue(newState)
        } catch (_: Exception) {
            // Manejo seguro para pruebas unitarias en entorno JVM puro
        }
    }
}
