package com.example.bookstore_android.presentation.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore_android.R
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.Executors

class ReportsActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvSalesCount: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var layoutSalesList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        initViews()
        loadSalesReport()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarReports)
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
        tvSalesCount = findViewById(R.id.tvSalesCount)
        pbLoading = findViewById(R.id.pbReportsLoading)
        layoutSalesList = findViewById(R.id.layoutSalesList)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadSalesReport() {
        pbLoading.visibility = View.VISIBLE
        Executors.newSingleThreadExecutor().execute {
            try {
                val url = URL("https://bookstorebackend-production-f7b2.up.railway.app/api/sales")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                val salesJson = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use {
                    it.readText()
                }
                connection.disconnect()

                val salesArray = JSONArray(salesJson)
                var totalAmount = 0.0

                val salesList = mutableListOf<SaleItem>()
                for (i in 0 until salesArray.length()) {
                    val s = salesArray.getJSONObject(i)
                    val idSale = s.optLong("idSale", i + 1L)
                    val date = s.optString("date", "Sin fecha")
                    val total = s.optDouble("total", 0.0)
                    totalAmount += total
                    salesList.add(SaleItem(idSale, date, total))
                }

                runOnUiThread {
                    pbLoading.visibility = View.GONE
                    tvTotalRevenue.text = formatCurrencyCop(totalAmount)
                    tvSalesCount.text = "${salesList.size} ventas en el historial"
                    renderSales(salesList)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    pbLoading.visibility = View.GONE
                    tvSalesCount.text = "No se pudieron cargar los reportes: ${e.message}"
                }
            }
        }
    }

    private fun renderSales(sales: List<SaleItem>) {
        layoutSalesList.removeAllViews()
        for (sale in sales) {
            val itemView = layoutInflater.inflate(R.layout.item_sale_card, layoutSalesList, false)
            val tvSaleId = itemView.findViewById<TextView>(R.id.tvSaleNumber)
            val tvSaleDate = itemView.findViewById<TextView>(R.id.tvSaleDate)
            val tvSaleTotal = itemView.findViewById<TextView>(R.id.tvSaleTotal)

            tvSaleId.text = "Venta #${sale.id}"
            tvSaleDate.text = sale.date.replace("T", " ")
            tvSaleTotal.text = formatCurrencyCop(sale.total)

            layoutSalesList.addView(itemView)
        }
    }

    private fun formatCurrencyCop(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))
        format.maximumFractionDigits = 0
        return format.format(value)
    }

    data class SaleItem(val id: Long, val date: String, val total: Double)
}
