package com.example.bookstore_android.data.repository

import com.example.bookstore_android.data.adapter.NetworkAdapter
import com.example.bookstore_android.data.adapter.SessionStorage
import com.example.bookstore_android.data.adapter.UserNetworkAdapter
import com.example.bookstore_android.data.remote.LoginApi
import com.example.bookstore_android.domain.model.LoginCredentials
import com.example.bookstore_android.domain.model.User
import com.example.bookstore_android.domain.repository.LoginRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Patrón Repository:
 * Orquesta la obtención y persistencia de datos, abstrayendo a la capa de
 * presentación de si los datos provienen de la API remota o del almacenamiento local.
 */
class LoginRepositoryImpl(
    private val loginApi: LoginApi,
    private val sessionStorage: SessionStorage,
    private val userAdapter: NetworkAdapter<User> = UserNetworkAdapter(),
    private val backgroundExecutor: Executor = Executors.newSingleThreadExecutor()
) : LoginRepository {

    override fun login(credentials: LoginCredentials, callback: (Result<User>) -> Unit) {
        backgroundExecutor.execute {
            try {
                val rawJson = loginApi.login(credentials.email, credentials.password)
                val user = userAdapter.adaptFromJson(rawJson)
                sessionStorage.saveUser(user)
                callback(Result.success(user))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    override fun getSavedUser(): User? = sessionStorage.getSavedUser()

    override fun saveUser(user: User) = sessionStorage.saveUser(user)

    override fun clearSession() = sessionStorage.clearSession()
}
