package com.example.bookstore_android.data.remote

import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HttpDashboardApiClient(
    private val baseUrl: String = "https://bookstorebackend-production-f7b2.up.railway.app"
) : DashboardApi {

    override fun fetchStats(): DashboardStatsDto {
        val booksJson = makeGetRequest("$baseUrl/api/books")
        val clientsJson = makeGetRequest("$baseUrl/api/clients")
        val salesJson = makeGetRequest("$baseUrl/api/sales")

        val booksArray = JSONArray(booksJson)
        val clientsArray = JSONArray(clientsJson)
        val salesArray = JSONArray(salesJson)

        val totalBooks = booksArray.length()
        val totalClients = clientsArray.length()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        var salesToday = 0.0
        var transactionsToday = 0

        for (i in 0 until salesArray.length()) {
            val sale = salesArray.getJSONObject(i)
            val date = sale.optString("date", "")
            val total = sale.optDouble("total", 0.0)

            if (date.startsWith(todayStr)) {
                salesToday += total
                transactionsToday++
            }
        }

        // Cálculo estricto de las ventas y transacciones ocurridas en el día de hoy
        return DashboardStatsDto(
            totalBooks = totalBooks,
            totalClients = totalClients,
            salesToday = salesToday,
            transactionsToday = transactionsToday
        )
    }

    private fun makeGetRequest(urlString: String): String {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                connectTimeout = 10000
                readTimeout = 10000
            }

            val statusCode = connection.responseCode
            if (statusCode == HttpURLConnection.HTTP_OK) {
                return BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use {
                    it.readText()
                }
            } else {
                throw Exception("Error HTTP $statusCode al consultar $urlString")
            }
        } finally {
            connection?.disconnect()
        }
    }
}
