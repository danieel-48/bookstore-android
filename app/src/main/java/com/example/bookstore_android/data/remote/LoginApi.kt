package com.example.bookstore_android.data.remote

/**
 * Capa de Datos Remotos:
 * Define el contrato de comunicación con la API del backend de BookStore.
 */
interface LoginApi {
    @Throws(Exception::class)
    fun login(email: String, password: String): String
}
