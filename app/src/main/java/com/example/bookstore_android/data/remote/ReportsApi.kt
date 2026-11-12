package com.example.bookstore_android.data.remote

import com.example.bookstore_android.domain.model.BookItem
import com.example.bookstore_android.domain.model.ClientItem
import com.example.bookstore_android.domain.model.SaleDetail
import com.example.bookstore_android.domain.model.SaleRecord

data class RawReportData(
    val sales: List<SaleRecord>,
    val details: List<SaleDetail>,
    val books: List<BookItem>,
    val clients: List<ClientItem>
)

interface ReportsApi {
    @Throws(Exception::class)
    fun fetchReportData(): RawReportData
}
