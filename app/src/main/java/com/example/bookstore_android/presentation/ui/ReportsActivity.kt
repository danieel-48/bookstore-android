package com.example.bookstore_android.presentation.ui

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bookstore_android.R
import com.example.bookstore_android.data.adapter.ReportProcessorAdapter
import com.example.bookstore_android.data.remote.HttpReportsApiClient
import com.example.bookstore_android.data.repository.ReportsRepositoryImpl
import com.example.bookstore_android.domain.model.BookItem
import com.example.bookstore_android.domain.model.ClientItem
import com.example.bookstore_android.domain.model.ReportPeriod
import com.example.bookstore_android.domain.model.ReportResult
import com.example.bookstore_android.domain.model.Sale
import com.example.bookstore_android.domain.model.getTodayLocalDate
import com.example.bookstore_android.presentation.viewmodel.ReportsUiState
import com.example.bookstore_android.presentation.viewmodel.ReportsViewModel
import com.example.bookstore_android.presentation.viewmodel.ReportsViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ReportsActivity : AppCompatActivity() {

    private val viewModel: ReportsViewModel by viewModels {
        val api = HttpReportsApiClient()
        val adapter = ReportProcessorAdapter()
        val repository = ReportsRepositoryImpl(api, adapter)
        ReportsViewModelFactory(repository)
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnPeriodDaily: Button
    private lateinit var btnPeriodMonthly: Button
    private lateinit var btnPeriodYearly: Button

    private lateinit var etFilterDate: EditText
    private lateinit var spClientFilter: Spinner
    private lateinit var spBookFilter: Spinner
    private lateinit var btnClearFilters: Button

    private lateinit var tvTotalRevenue: TextView
    private lateinit var tvSalesCount: TextView
    private lateinit var layoutSummaryItems: LinearLayout
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var layoutSalesList: LinearLayout

    private var clientsList: List<ClientItem> = emptyList()
    private var booksList: List<BookItem> = emptyList()
    private var isSpinnersPopulated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbarReports)
        btnPeriodDaily = findViewById(R.id.btnPeriodDaily)
        btnPeriodMonthly = findViewById(R.id.btnPeriodMonthly)
        btnPeriodYearly = findViewById(R.id.btnPeriodYearly)

        etFilterDate = findViewById(R.id.etFilterDate)
        spClientFilter = findViewById(R.id.spClientFilter)
        spBookFilter = findViewById(R.id.spBookFilter)
        btnClearFilters = findViewById(R.id.btnClearFilters)

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue)
        tvSalesCount = findViewById(R.id.tvSalesCount)
        layoutSummaryItems = findViewById(R.id.layoutSummaryItems)
        pbLoading = findViewById(R.id.pbReportsLoading)
        tvError = findViewById(R.id.tvReportsError)
        layoutSalesList = findViewById(R.id.layoutSalesList)

        etFilterDate.setText(viewModel.currentFilters.date ?: getTodayLocalDate())

        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        btnPeriodDaily.setOnClickListener {
            viewModel.setPeriod(ReportPeriod.DAILY)
        }
        btnPeriodMonthly.setOnClickListener {
            viewModel.setPeriod(ReportPeriod.MONTHLY)
        }
        btnPeriodYearly.setOnClickListener {
            viewModel.setPeriod(ReportPeriod.YEARLY)
        }

        etFilterDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnClearFilters.setOnClickListener {
            if (isSpinnersPopulated) {
                spClientFilter.setSelection(0)
                spBookFilter.setSelection(0)
            }
            val today = getTodayLocalDate()
            etFilterDate.setText(today)
            viewModel.clearFilters()
        }
    }

    private fun showDatePickerDialog() {
        val currentDateStr = etFilterDate.text.toString().ifBlank {
            viewModel.currentFilters.date ?: getTodayLocalDate()
        }
        val parts = currentDateStr.split("-")
        val year: Int
        val month: Int
        val day: Int
        if (parts.size == 3) {
            year = parts[0].toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            month = (parts[1].toIntOrNull()?.minus(1)) ?: Calendar.getInstance().get(Calendar.MONTH)
            day = parts[2].toIntOrNull() ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        } else {
            val cal = Calendar.getInstance()
            year = cal.get(Calendar.YEAR)
            month = cal.get(Calendar.MONTH)
            day = cal.get(Calendar.DAY_OF_MONTH)
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedMonth = String.format(Locale.US, "%02d", selectedMonth + 1)
                val formattedDay = String.format(Locale.US, "%02d", selectedDay)
                val formattedDate = "$selectedYear-$formattedMonth-$formattedDay"
                etFilterDate.setText(formattedDate)
                viewModel.setDateFilter(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Limpiar") { _, _ ->
            etFilterDate.setText("")
            viewModel.setDateFilter(null)
        }

        datePickerDialog.show()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is ReportsUiState.Loading -> {
                    pbLoading.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                }
                is ReportsUiState.Success -> {
                    pbLoading.visibility = View.GONE
                    tvError.visibility = View.GONE
                    updatePeriodButtons(state.currentFilters.period)
                    val expectedDateText = state.currentFilters.date ?: ""
                    if (etFilterDate.text.toString() != expectedDateText) {
                        etFilterDate.setText(expectedDateText)
                    }
                    setupSpinners(state.clients, state.books)
                    renderReportResult(state.result)
                }
                is ReportsUiState.Error -> {
                    pbLoading.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                }
            }
        }
    }

    private fun updatePeriodButtons(currentPeriod: ReportPeriod) {
        val selectedBg = ContextCompat.getColor(this, R.color.slate_900)
        val defaultBg = ContextCompat.getColor(this, R.color.slate_100)
        val selectedTextColor = ContextCompat.getColor(this, R.color.white)
        val defaultTextColor = ContextCompat.getColor(this, R.color.slate_700)

        btnPeriodDaily.setBackgroundColor(if (currentPeriod == ReportPeriod.DAILY) selectedBg else defaultBg)
        btnPeriodDaily.setTextColor(if (currentPeriod == ReportPeriod.DAILY) selectedTextColor else defaultTextColor)

        btnPeriodMonthly.setBackgroundColor(if (currentPeriod == ReportPeriod.MONTHLY) selectedBg else defaultBg)
        btnPeriodMonthly.setTextColor(if (currentPeriod == ReportPeriod.MONTHLY) selectedTextColor else defaultTextColor)

        btnPeriodYearly.setBackgroundColor(if (currentPeriod == ReportPeriod.YEARLY) selectedBg else defaultBg)
        btnPeriodYearly.setTextColor(if (currentPeriod == ReportPeriod.YEARLY) selectedTextColor else defaultTextColor)
    }

    private fun setupSpinners(clients: List<ClientItem>, books: List<BookItem>) {
        if (isSpinnersPopulated) return

        clientsList = clients
        booksList = books

        val clientNames = mutableListOf("Todos los clientes").apply {
            addAll(clients.map { it.name })
        }
        val clientAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, clientNames)
        spClientFilter.adapter = clientAdapter

        val bookTitles = mutableListOf("Todos los libros").apply {
            addAll(books.map { it.title })
        }
        val bookAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bookTitles)
        spBookFilter.adapter = bookAdapter

        spClientFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedClient = if (position > 0) clientsList[position - 1].idClient else null
                if (selectedClient != viewModel.currentFilters.idClient) {
                    viewModel.setClientFilter(selectedClient)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spBookFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedBook = if (position > 0) booksList[position - 1].id else null
                if (selectedBook != viewModel.currentFilters.idBook) {
                    viewModel.setBookFilter(selectedBook)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        isSpinnersPopulated = true
    }

    private fun renderReportResult(result: ReportResult) {
        tvTotalRevenue.text = formatCurrencyCop(result.totalAmount)
        tvSalesCount.text = "${result.sales.size} ventas en el reporte"

        // Desglose de períodos del resumen
        layoutSummaryItems.removeAllViews()
        for (summary in result.summary) {
            val tv = TextView(this).apply {
                text = "${summary.period}  ·  Ventas: ${summary.salesCount}  ·  Total: ${formatCurrencyCop(summary.totalAmount)}"
                setTextColor(ContextCompat.getColor(context, R.color.slate_700))
                textSize = 13f
                setPadding(0, 6, 0, 6)
            }
            layoutSummaryItems.addView(tv)
        }

        // Listado de tarjetas de ventas
        layoutSalesList.removeAllViews()
        if (result.sales.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "No se encontraron ventas para los filtros seleccionados."
                setTextColor(ContextCompat.getColor(context, R.color.slate_500))
                textSize = 14f
                setPadding(16, 24, 16, 24)
            }
            layoutSalesList.addView(tvEmpty)
            return
        }

        for (sale in result.sales) {
            val cardView = layoutInflater.inflate(R.layout.item_sale_card, layoutSalesList, false)
            val tvSaleNumber = cardView.findViewById<TextView>(R.id.tvSaleNumber)
            val tvSaleTotal = cardView.findViewById<TextView>(R.id.tvSaleTotal)
            val tvSaleClient = cardView.findViewById<TextView>(R.id.tvSaleClient)
            val tvSaleDate = cardView.findViewById<TextView>(R.id.tvSaleDate)
            val btnViewDetails = cardView.findViewById<Button>(R.id.btnViewDetails)

            tvSaleNumber.text = "Comprobante: ${sale.saleNumber}"
            tvSaleTotal.text = formatCurrencyCop(sale.total)
            tvSaleClient.text = "Cliente: ${sale.clientName}"
            tvSaleDate.text = "Fecha: ${sale.date.replace("T", " ")}"

            btnViewDetails.setOnClickListener {
                showSaleDetailsModal(sale)
            }

            layoutSalesList.addView(cardView)
        }
    }

    // Modal de Detalle de Venta idéntico al modal del frontend (HU-07)
    private fun showSaleDetailsModal(sale: Sale) {
        val itemsMessage = StringBuilder().apply {
            append("Venta #${sale.idSale}\n")
            append("Número de comprobante: ${sale.saleNumber}\n")
            append("Fecha: ${sale.date.replace("T", " ")}\n")
            append("Cliente: ${sale.clientName}\n\n")
            append("--------------------------------------\n")
            append("DETALLE DE PRODUCTOS:\n")
            append("--------------------------------------\n")

            if (sale.items.isEmpty()) {
                append("No se registraron productos individuales.\n")
            } else {
                for (item in sale.items) {
                    append("• ${item.bookTitle}\n")
                    append("  Cantidad: ${item.quantity} x ${formatCurrencyCop(item.unitPrice)}\n")
                    append("  Subtotal: ${formatCurrencyCop(item.total)}\n\n")
                }
            }
            append("--------------------------------------\n")
            append("TOTAL PAGADO: ${formatCurrencyCop(sale.total)}")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Detalle de la Venta #${sale.idSale}")
            .setMessage(itemsMessage.toString())
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun formatCurrencyCop(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))
        format.maximumFractionDigits = 0
        return format.format(value)
    }
}
