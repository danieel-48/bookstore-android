package com.example.bookstore_android.data.remote

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiException(val statusCode: Int, message: String) : Exception(message)

/**
 * Cliente HTTP que se conecta al servicio REST en Railway.
 */
class HttpLoginApiClient(
    private val endpointUrl: String = "https://bookstorebackend-production-f7b2.up.railway.app/api/auth/login"
) : LoginApi {

    override fun login(email: String, password: String): String {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                doInput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            val payload = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val statusCode = connection.responseCode

            if (statusCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                return reader.use { it.readText() }
            } else {
                val stream = connection.errorStream ?: connection.inputStream
                val errorBody = stream?.let {
                    BufferedReader(InputStreamReader(it, "UTF-8")).use { r -> r.readText() }
                } ?: ""

                val message = when (statusCode) {
                    HttpURLConnection.HTTP_UNAUTHORIZED -> "Correo o contraseña incorrectos."
                    HttpURLConnection.HTTP_FORBIDDEN -> "El usuario está inactivo."
                    else -> {
                        try {
                            val json = JSONObject(errorBody)
                            json.optString("message", "Error al iniciar sesión (${statusCode}).")
                        } catch (e: Exception) {
                            "Error al iniciar sesión (${statusCode})."
                        }
                    }
                }
                throw ApiException(statusCode, message)
            }
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException(0, "No se pudo conectar con el servidor API. Verifique su conexión.")
        } finally {
            connection?.disconnect()
        }
    }
}
