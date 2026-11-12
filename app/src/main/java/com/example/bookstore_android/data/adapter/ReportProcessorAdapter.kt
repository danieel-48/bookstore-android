package com.example.bookstore_android.data.adapter

import com.example.bookstore_android.data.remote.RawReportData
import com.example.bookstore_android.domain.model.ReportFilters
import com.example.bookstore_android.domain.model.ReportResult
import com.example.bookstore_android.domain.model.ReportSummary
import com.example.bookstore_android.domain.model.Sale
import com.example.bookstore_android.domain.model.SaleItem

/**
 * Patrón Adapter:
 * Procesa y adapta los datos crudos de la API (ventas, detalles, libros y clientes)
 * a un ReportResult estructurado y agrupado por períodos (Diario, Mensual, Anual)
 * con soporte de filtros idéntico a buildReport() del frontend.
 */
interface ReportAdapter {
    fun process(rawData: RawReportData, filters: ReportFilters): ReportResult
}

class ReportProcessorAdapter : ReportAdapter {

    override fun process(rawData: RawReportData, filters: ReportFilters): ReportResult {
        val periodLength = filters.period.dateLength
        val periodOf = { date: String ->
            if (date.length >= periodLength) date.substring(0, periodLength) else date
        }

        val selectedPeriod = filters.date?.let { periodOf(it) } ?: ""

        // 1. Filtrar ventas por período, libro y cliente
        val filteredSales = rawData.sales
            .filter { it.date.isNotEmpty() }
            .filter { selectedPeriod.isEmpty() || periodOf(it.date) == selectedPeriod }
            .filter { filters.idClient == null || it.idClient == filters.idClient }
            .filter { filters.idBook == null || rawData.details.any { d -> d.idSale == it.idSale && d.idBook == filters.idBook } }
            .sortedByDescending { it.date }
            .map { record ->
                val client = rawData.clients.find { it.idClient == record.idClient }
                val clientName = client?.name ?: (record.idClient?.let { "Cliente #$it" } ?: "Cliente no asignado")

                val items = rawData.details
                    .filter { it.idSale == record.idSale }
                    .map { detail ->
                        val book = rawData.books.find { it.id == detail.idBook }
                        val bookTitle = book?.title ?: "Libro #${detail.idBook}"
                        val unitPrice = if (detail.quantity > 0) detail.total / detail.quantity else 0.0
                        SaleItem(
                            idDetail = detail.idDetail,
                            idBook = detail.idBook,
                            bookTitle = bookTitle,
                            quantity = detail.quantity,
                            unitPrice = unitPrice,
                            total = detail.total
                        )
                    }

                Sale(
                    idSale = record.idSale,
                    date = record.date,
                    total = record.total,
                    clientName = clientName,
                    userName = "Vendedor #${record.idUser ?: 1}",
                    items = items
                )
            }

        // 2. Agrupar y calcular totales por período (Diario, Mensual o Anual)
        val grouped = linkedMapOf<String, ReportSummary>()
        for (sale in filteredSales) {
            val period = periodOf(sale.date)
            val current = grouped[period] ?: ReportSummary(period, 0, 0.0)
            grouped[period] = current.copy(
                salesCount = current.salesCount + 1,
                totalAmount = current.totalAmount + sale.total
            )
        }

        val summaryList = grouped.values.sortedBy { it.period }
        val totalAmount = filteredSales.sumOf { it.total }

        return ReportResult(
            summary = summaryList,
            sales = filteredSales,
            totalAmount = totalAmount
        )
    }
}
