package com.example.bookstore_android.data.repository

import com.example.bookstore_android.data.adapter.InMemorySessionAdapter
import com.example.bookstore_android.data.adapter.NetworkAdapter
import com.example.bookstore_android.data.remote.ApiException
import com.example.bookstore_android.data.remote.LoginApi
import com.example.bookstore_android.domain.model.LoginCredentials
import com.example.bookstore_android.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor

/**
 * Pruebas unitarias con JUnit del LoginRepositoryImpl (Patrón Repository).
 */
class LoginRepositoryTest {

    private lateinit var fakeApi: FakeLoginApi
    private lateinit var inMemoryStorage: InMemorySessionAdapter
    private lateinit var fakeAdapter: FakeUserAdapter
    private lateinit var directExecutor: Executor
    private lateinit var repository: LoginRepositoryImpl

    private val sampleUser = User(
        id = 1L,
        name = "Ana Ariza",
        email = "ana@test.com",
        role = "ADMIN",
        active = true
    )

    @Before
    fun setUp() {
        fakeApi = FakeLoginApi()
        inMemoryStorage = InMemorySessionAdapter()
        fakeAdapter = FakeUserAdapter(sampleUser)
        // Ejecutor síncrono para que las pruebas corran en el mismo hilo sin asincronía
        directExecutor = Executor { command -> command.run() }

        repository = LoginRepositoryImpl(
            loginApi = fakeApi,
            sessionStorage = inMemoryStorage,
            userAdapter = fakeAdapter,
            backgroundExecutor = directExecutor
        )
    }

    @Test
    fun login_whenApiReturnsSuccess_savesSessionAndReturnsUser() {
        fakeApi.jsonResponse = "{\"id\":1,\"name\":\"Ana Ariza\",\"email\":\"ana@test.com\",\"role\":\"ADMIN\",\"active\":true}"

        var resultUser: User? = null
        repository.login(LoginCredentials("ana@test.com", "Admin123")) { result ->
            resultUser = result.getOrNull()
        }

        assertNotNull(resultUser)
        assertEquals("Ana Ariza", resultUser?.name)
        assertEquals(sampleUser, inMemoryStorage.getSavedUser())
    }

    @Test
    fun login_whenApiThrowsException_returnsFailure() {
        fakeApi.exceptionToThrow = ApiException(401, "Correo o contraseña incorrectos.")

        var failureMessage: String? = null
        repository.login(LoginCredentials("ana@test.com", "wrong")) { result ->
            failureMessage = result.exceptionOrNull()?.message
        }

        assertEquals("Correo o contraseña incorrectos.", failureMessage)
        assertNull(inMemoryStorage.getSavedUser())
    }

    @Test
    fun clearSession_removesUserFromStorage() {
        inMemoryStorage.saveUser(sampleUser)
        assertEquals(sampleUser, repository.getSavedUser())

        repository.clearSession()
        assertNull(repository.getSavedUser())
    }

    private class FakeLoginApi : LoginApi {
        var jsonResponse: String = "{}"
        var exceptionToThrow: Exception? = null

        override fun login(email: String, password: String): String {
            exceptionToThrow?.let { throw it }
            return jsonResponse
        }
    }

    private class FakeUserAdapter(private val defaultUser: User) : NetworkAdapter<User> {
        override fun adaptFromJson(rawJson: String): User = defaultUser
        override fun adaptToJson(entity: User): String = "{}"
    }
}
