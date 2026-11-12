package com.example.bookstore_android.domain.repository

import com.example.bookstore_android.domain.model.BookItem
import com.example.bookstore_android.domain.model.ClientItem
import com.example.bookstore_android.domain.model.ReportFilters
import com.example.bookstore_android.domain.model.ReportResult

/**
 * Patrón Repository (Dominio):
 * Contrato para la consulta y generación de reportes de ventas.
 */
interface ReportsRepository {
    fun getReports(filters: ReportFilters, callback: (Result<ReportResult>) -> Unit)
    fun getFilterOptions(callback: (Result<Pair<List<ClientItem>, List<BookItem>>>) -> Unit)
}
