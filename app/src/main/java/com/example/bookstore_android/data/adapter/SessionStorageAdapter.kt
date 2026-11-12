package com.example.bookstore_android.data.adapter

import android.content.Context
import android.content.SharedPreferences
import com.example.bookstore_android.domain.model.User

/**
 * Patrón Adapter:
 * Adapta el mecanismo de almacenamiento local (SharedPreferences en Android, o en memoria)
 * a una interfaz limpia para el Repositorio.
 */
interface SessionStorage {
    fun saveUser(user: User)
    fun getSavedUser(): User?
    fun clearSession()
}

class SharedPrefsSessionAdapter(
    private val contextProvider: () -> Context?,
    private val userAdapter: NetworkAdapter<User> = UserNetworkAdapter()
) : SessionStorage {

    companion object {
        private const val PREFS_NAME = "bookstore_auth_prefs"
        private const val KEY_SAVED_USER = "key_saved_user_json"
    }

    private fun getPrefs(): SharedPreferences? {
        return contextProvider()?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun saveUser(user: User) {
        val json = userAdapter.adaptToJson(user)
        getPrefs()?.edit()?.putString(KEY_SAVED_USER, json)?.apply()
    }

    override fun getSavedUser(): User? {
        val raw = getPrefs()?.getString(KEY_SAVED_USER, null) ?: return null
        return try {
            userAdapter.adaptFromJson(raw)
        } catch (e: Exception) {
            null
        }
    }

    override fun clearSession() {
        getPrefs()?.edit()?.remove(KEY_SAVED_USER)?.apply()
    }
}

/**
 * Adaptador de almacenamiento en memoria (utilizado para pruebas unitarias).
 */
class InMemorySessionAdapter(
    private var inMemoryUser: User? = null
) : SessionStorage {
    override fun saveUser(user: User) {
        inMemoryUser = user
    }

    override fun getSavedUser(): User? = inMemoryUser

    override fun clearSession() {
        inMemoryUser = null
    }
}
