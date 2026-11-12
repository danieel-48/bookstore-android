package com.example.bookstore_android.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportPeriod(val label: String, val dateLength: Int) {
    DAILY("Diario", 10),
    MONTHLY("Mensual", 7),
    YEARLY("Anual", 4)
}

fun getTodayLocalDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}

data class SaleRecord(
    val idSale: Long,
    val date: String,
    val total: Double,
    val idClient: Long?,
    val idUser: Long?
)

data class SaleDetail(
    val idDetail: Long,
    val idSale: Long,
    val idBook: Long,
    val quantity: Int,
    val total: Double
)

data class SaleItem(
    val idDetail: Long,
    val idBook: Long,
    val bookTitle: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double
)

data class Sale(
    val idSale: Long,
    val date: String,
    val total: Double,
    val clientName: String,
    val userName: String,
    val items: List<SaleItem>
) {
    val saleNumber: String
        get() {
            if (date.length >= 10) {
                val cleanDate = date.substring(0, 10).replace("-", "")
                return "$cleanDate-$idSale"
            }
            return idSale.toString()
        }
}

data class ReportSummary(
    val period: String,
    val salesCount: Int,
    val totalAmount: Double
)

data class ReportFilters(
    val period: ReportPeriod = ReportPeriod.DAILY,
    val date: String? = getTodayLocalDate(),
    val idClient: Long? = null,
    val idBook: Long? = null
)

data class ReportResult(
    val summary: List<ReportSummary>,
    val sales: List<Sale>,
    val totalAmount: Double
)

data class BookItem(
    val id: Long,
    val title: String,
    val price: Double
)

data class ClientItem(
    val idClient: Long,
    val name: String,
    val idNumber: Long
)
