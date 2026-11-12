package com.example.bookstore_android.presentation.viewmodel

import com.example.bookstore_android.domain.model.LoginCredentials
import com.example.bookstore_android.domain.model.User
import com.example.bookstore_android.domain.repository.LoginRepository
import com.example.bookstore_android.domain.validator.LoginValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias con JUnit del LoginViewModel (Patrón MVVM).
 */
class LoginViewModelTest {

    private lateinit var fakeRepository: FakeLoginRepository
    private lateinit var viewModel: LoginViewModel

    private val sampleUser = User(
        id = 1L,
        name = "Ana Ariza",
        email = "ana@test.com",
        role = "ADMIN",
        active = true
    )

    @Before
    fun setUp() {
        fakeRepository = FakeLoginRepository()
        viewModel = LoginViewModel(fakeRepository, LoginValidator())
    }

    @Test
    fun initialState_withoutSavedUser_isInitial() {
        assertTrue(viewModel.currentState is LoginUiState.Initial)
    }

    @Test
    fun init_withExistingSavedUser_emitsSuccessState() {
        val repoWithUser = FakeLoginRepository(initialSavedUser = sampleUser)
        val vm = LoginViewModel(repoWithUser, LoginValidator())

        assertTrue(vm.currentState is LoginUiState.Success)
        val successState = vm.currentState as LoginUiState.Success
        assertEquals("Ana Ariza", successState.user.name)
        assertEquals("ADMIN", successState.user.role)
    }

    @Test
    fun login_withEmptyEmail_emitsValidationError() {
        viewModel.login("", "Admin123")

        assertTrue(viewModel.currentState is LoginUiState.Error)
        val errorState = viewModel.currentState as LoginUiState.Error
        assertEquals("El correo electrónico es obligatorio.", errorState.message)
    }

    @Test
    fun login_withInvalidEmailFormat_emitsValidationError() {
        viewModel.login("correo-invalido", "Admin123")

        assertTrue(viewModel.currentState is LoginUiState.Error)
        val errorState = viewModel.currentState as LoginUiState.Error
        assertEquals("Ingrese un correo electrónico válido.", errorState.message)
    }

    @Test
    fun login_withShortPassword_emitsValidationError() {
        viewModel.login("ana@test.com", "123")

        assertTrue(viewModel.currentState is LoginUiState.Error)
        val errorState = viewModel.currentState as LoginUiState.Error
        assertEquals("La contraseña debe tener al menos 6 caracteres.", errorState.message)
    }

    @Test
    fun login_withValidCredentials_whenApiSucceeds_emitsSuccessState() {
        fakeRepository.userToReturn = sampleUser

        viewModel.login("ana@test.com", "Admin123")

        assertTrue(viewModel.currentState is LoginUiState.Success)
        val successState = viewModel.currentState as LoginUiState.Success
        assertEquals(sampleUser.email, successState.user.email)
        assertEquals(sampleUser.name, successState.user.name)
        assertEquals(sampleUser, fakeRepository.getSavedUser())
    }

    @Test
    fun login_withBadCredentials_whenApiReturns401_emitsErrorState() {
        fakeRepository.errorToThrow = Exception("Correo o contraseña incorrectos.")

        viewModel.login("ana@test.com", "PasswordInvalida")

        assertTrue(viewModel.currentState is LoginUiState.Error)
        val errorState = viewModel.currentState as LoginUiState.Error
        assertEquals("Correo o contraseña incorrectos.", errorState.message)
    }

    @Test
    fun logout_clearsSessionAndResetsToInitialState() {
        fakeRepository.saveUser(sampleUser)
        viewModel.checkExistingSession()
        assertTrue(viewModel.currentState is LoginUiState.Success)

        viewModel.logout()

        assertTrue(viewModel.currentState is LoginUiState.Initial)
        assertNull(fakeRepository.getSavedUser())
    }

    /**
     * Fake Repository para aislamiento en pruebas unitarias puras.
     */
    private class FakeLoginRepository(
        var userToReturn: User? = null,
        var errorToThrow: Exception? = null,
        initialSavedUser: User? = null
    ) : LoginRepository {

        private var currentSavedUser: User? = initialSavedUser

        override fun login(credentials: LoginCredentials, callback: (Result<User>) -> Unit) {
            val error = errorToThrow
            val user = userToReturn
            if (error != null) {
                callback(Result.failure(error))
            } else if (user != null) {
                currentSavedUser = user
                callback(Result.success(user))
            }
        }

        override fun getSavedUser(): User? = currentSavedUser

        override fun saveUser(user: User) {
            currentSavedUser = user
        }

        override fun clearSession() {
            currentSavedUser = null
        }
    }
}
