package com.example.bookstore_android.domain.validator

import com.example.bookstore_android.domain.model.LoginCredentials
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias con JUnit de la lógica de validación del Login.
 */
class LoginValidatorTest {

    private lateinit var validator: LoginValidator

    @Before
    fun setUp() {
        validator = LoginValidator()
    }

    @Test
    fun validateEmail_withEmptyEmail_returnsError() {
        val result = validator.validateEmail("   ")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("El correo electrónico es obligatorio.", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun validateEmail_withInvalidFormat_returnsError() {
        val result = validator.validateEmail("correo-sin-arroba.com")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Ingrese un correo electrónico válido.", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun validateEmail_withValidFormat_returnsValid() {
        val result = validator.validateEmail("ana@test.com")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun validatePassword_withEmptyPassword_returnsError() {
        val result = validator.validatePassword("")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("La contraseña es obligatoria.", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun validatePassword_withLessThan6Characters_returnsError() {
        val result = validator.validatePassword("12345")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("La contraseña debe tener al menos 6 caracteres.", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun validatePassword_withValidPassword_returnsValid() {
        val result = validator.validatePassword("Admin123")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun validate_withValidCredentials_returnsValid() {
        val credentials = LoginCredentials("ana@test.com", "Admin123")
        val result = validator.validate(credentials)
        assertTrue(result is ValidationResult.Valid)
    }
}
