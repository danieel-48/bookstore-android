package com.example.bookstore_android.presentation.viewmodel

import com.example.bookstore_android.domain.model.DashboardStats
import com.example.bookstore_android.domain.model.LoginCredentials
import com.example.bookstore_android.domain.model.User
import com.example.bookstore_android.domain.repository.DashboardRepository
import com.example.bookstore_android.domain.repository.LoginRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias con JUnit del DashboardViewModel (Patrón MVVM).
 */
class DashboardViewModelTest {

    private lateinit var fakeDashboardRepo: FakeDashboardRepository
    private lateinit var fakeLoginRepo: FakeLoginRepository
    private lateinit var viewModel: DashboardViewModel

    private val sampleStats = DashboardStats(
        totalBooks = 15,
        totalClients = 42,
        salesToday = 150000.0,
        transactionsToday = 5
    )

    private val sampleUser = User(
        id = 1L,
        name = "Ana Ariza",
        email = "ana@test.com",
        role = "ADMIN",
        active = true
    )

    @Before
    fun setUp() {
        fakeDashboardRepo = FakeDashboardRepository(statsToReturn = sampleStats)
        fakeLoginRepo = FakeLoginRepository(savedUser = sampleUser)
        viewModel = DashboardViewModel(fakeDashboardRepo, fakeLoginRepo)
    }

    @Test
    fun init_loadsStatsSuccessfully() {
        assertTrue(viewModel.currentState is DashboardUiState.Success)
        val successState = viewModel.currentState as DashboardUiState.Success
        assertEquals(15, successState.stats.totalBooks)
        assertEquals(42, successState.stats.totalClients)
        assertEquals(150000.0, successState.stats.salesToday, 0.001)
        assertEquals(5, successState.stats.transactionsToday)
    }

    @Test
    fun loadStats_whenRepositoryFails_emitsErrorState() {
        fakeDashboardRepo.errorToThrow = Exception("Error de conexión a la API")
        viewModel.loadStats()

        assertTrue(viewModel.currentState is DashboardUiState.Error)
        val errorState = viewModel.currentState as DashboardUiState.Error
        assertEquals("Error de conexión a la API", errorState.message)
    }

    @Test
    fun getLoggedUser_returnsUserFromLoginRepository() {
        val user = viewModel.getLoggedUser()
        assertEquals("Ana Ariza", user?.name)
        assertEquals("ADMIN", user?.role)
    }

    @Test
    fun logout_clearsUserSession() {
        viewModel.logout()
        assertNull(fakeLoginRepo.getSavedUser())
    }

    private class FakeDashboardRepository(
        var statsToReturn: DashboardStats? = null,
        var errorToThrow: Exception? = null
    ) : DashboardRepository {

        override fun getStats(callback: (Result<DashboardStats>) -> Unit) {
            val error = errorToThrow
            val stats = statsToReturn
            if (error != null) {
                callback(Result.failure(error))
            } else if (stats != null) {
                callback(Result.success(stats))
            }
        }
    }

    private class FakeLoginRepository(
        private var savedUser: User? = null
    ) : LoginRepository {
        override fun login(credentials: LoginCredentials, callback: (Result<User>) -> Unit) {}
        override fun getSavedUser(): User? = savedUser
        override fun saveUser(user: User) { savedUser = user }
        override fun clearSession() { savedUser = null }
    }
}
