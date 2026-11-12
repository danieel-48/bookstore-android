package com.example.bookstore_android.domain.repository

import com.example.bookstore_android.domain.model.LoginCredentials
import com.example.bookstore_android.domain.model.User

/**
 * Patrón Repository (Dominio):
 * Desacopla la lógica de presentación del origen de datos (remoto vs local).
 */
interface LoginRepository {
    fun login(credentials: LoginCredentials, callback: (Result<User>) -> Unit)
    fun getSavedUser(): User?
    fun saveUser(user: User)
    fun clearSession()
}
