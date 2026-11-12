package com.example.bookstore_android.domain.validator

import com.example.bookstore_android.domain.model.LoginCredentials

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

class LoginValidator {
    // Regex estándar para validación de formato de correo electrónico
    private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("El correo electrónico es obligatorio.")
        }
        if (!emailPattern.matches(trimmed)) {
            return ValidationResult.Invalid("Ingrese un correo electrónico válido.")
        }
        return ValidationResult.Valid
    }

    fun validatePassword(password: String): ValidationResult {
        val trimmed = password.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("La contraseña es obligatoria.")
        }
        if (trimmed.length < 6) {
            return ValidationResult.Invalid("La contraseña debe tener al menos 6 caracteres.")
        }
        return ValidationResult.Valid
    }

    fun validate(credentials: LoginCredentials): ValidationResult {
        val emailCheck = validateEmail(credentials.email)
        if (emailCheck is ValidationResult.Invalid) return emailCheck

        val passCheck = validatePassword(credentials.password)
        if (passCheck is ValidationResult.Invalid) return passCheck

        return ValidationResult.Valid
    }
}
