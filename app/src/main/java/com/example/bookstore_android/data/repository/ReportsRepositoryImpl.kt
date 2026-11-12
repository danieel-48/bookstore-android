package com.example.bookstore_android.data.repository

import com.example.bookstore_android.data.adapter.ReportAdapter
import com.example.bookstore_android.data.adapter.ReportProcessorAdapter
import com.example.bookstore_android.data.remote.RawReportData
import com.example.bookstore_android.data.remote.ReportsApi
import com.example.bookstore_android.domain.model.BookItem
import com.example.bookstore_android.domain.model.ClientItem
import com.example.bookstore_android.domain.model.ReportFilters
import com.example.bookstore_android.domain.model.ReportResult
import com.example.bookstore_android.domain.repository.ReportsRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ReportsRepositoryImpl(
    private val reportsApi: ReportsApi,
    private val reportAdapter: ReportAdapter = ReportProcessorAdapter(),
    private val backgroundExecutor: Executor = Executors.newSingleThreadExecutor()
) : ReportsRepository {

    private var cachedData: RawReportData? = null

    override fun getReports(filters: ReportFilters, callback: (Result<ReportResult>) -> Unit) {
        backgroundExecutor.execute {
            try {
                val data = cachedData ?: reportsApi.fetchReportData().also { cachedData = it }
                val result = reportAdapter.process(data, filters)
                callback(Result.success(result))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }

    override fun getFilterOptions(callback: (Result<Pair<List<ClientItem>, List<BookItem>>>) -> Unit) {
        backgroundExecutor.execute {
            try {
                val data = cachedData ?: reportsApi.fetchReportData().also { cachedData = it }
                callback(Result.success(Pair(data.clients, data.books)))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }
}
