package com.example.bookstore_android.data.adapter

import com.example.bookstore_android.domain.model.User
import org.json.JSONObject

/**
 * Patrón Adapter:
 * Adapta los datos crudos en formato JSON recibidos de la API remota
 * a la entidad de dominio puro User (y viceversa).
 */
interface NetworkAdapter<T> {
    fun adaptFromJson(rawJson: String): T
    fun adaptToJson(entity: T): String
}

class UserNetworkAdapter : NetworkAdapter<User> {

    override fun adaptFromJson(rawJson: String): User {
        val json = JSONObject(rawJson)
        return User(
            id = json.optLong("id", 0L),
            name = json.optString("name", "Usuario"),
            email = json.optString("email", ""),
            role = json.optString("role", "USUARIO"),
            active = json.optBoolean("active", true)
        )
    }

    override fun adaptToJson(entity: User): String {
        val json = JSONObject().apply {
            put("id", entity.id)
            put("name", entity.name)
            put("email", entity.email)
            put("role", entity.role)
            put("active", entity.active)
        }
        return json.toString()
    }
}
