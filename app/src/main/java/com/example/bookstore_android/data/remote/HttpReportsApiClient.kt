package com.example.bookstore_android.data.remote

import com.example.bookstore_android.domain.model.BookItem
import com.example.bookstore_android.domain.model.ClientItem
import com.example.bookstore_android.domain.model.SaleDetail
import com.example.bookstore_android.domain.model.SaleRecord
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpReportsApiClient(
    private val baseUrl: String = "https://bookstorebackend-production-f7b2.up.railway.app"
) : ReportsApi {

    override fun fetchReportData(): RawReportData {
        val salesJson = makeGetRequest("$baseUrl/api/sales")
        val detailsJson = makeGetRequest("$baseUrl/api/sale-details")
        val booksJson = makeGetRequest("$baseUrl/api/books")
        val clientsJson = makeGetRequest("$baseUrl/api/clients")

        val sales = parseSales(salesJson)
        val details = parseDetails(detailsJson)
        val books = parseBooks(booksJson)
        val clients = parseClients(clientsJson)

        return RawReportData(sales, details, books, clients)
    }

    private fun parseSales(json: String): List<SaleRecord> {
        val list = mutableListOf<SaleRecord>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SaleRecord(
                    idSale = obj.optLong("idSale", 0L),
                    date = obj.optString("date", ""),
                    total = obj.optDouble("total", 0.0),
                    idClient = if (obj.has("idClient") && !obj.isNull("idClient")) obj.optLong("idClient") else null,
                    idUser = if (obj.has("idUser") && !obj.isNull("idUser")) obj.optLong("idUser") else null
                )
            )
        }
        return list
    }

    private fun parseDetails(json: String): List<SaleDetail> {
        val list = mutableListOf<SaleDetail>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SaleDetail(
                    idDetail = obj.optLong("idDetail", 0L),
                    idSale = obj.optLong("idSale", 0L),
                    idBook = obj.optLong("idBook", 0L),
                    quantity = obj.optInt("quantity", 1),
                    total = obj.optDouble("total", 0.0)
                )
            )
        }
        return list
    }

    private fun parseBooks(json: String): List<BookItem> {
        val list = mutableListOf<BookItem>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                BookItem(
                    id = obj.optLong("id", 0L),
                    title = obj.optString("title", "Libro #${obj.optLong("id")}"),
                    price = obj.optDouble("price", 0.0)
                )
            )
        }
        return list
    }

    private fun parseClients(json: String): List<ClientItem> {
        val list = mutableListOf<ClientItem>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ClientItem(
                    idClient = obj.optLong("idClient", 0L),
                    name = obj.optString("name", "Cliente #${obj.optLong("idClient")}"),
                    idNumber = obj.optLong("idNumber", 0L)
                )
            )
        }
        return list
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
