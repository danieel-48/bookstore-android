package com.example.bookstore_android.data.adapter

import com.example.bookstore_android.data.remote.RawReportData
import com.example.bookstore_android.domain.model.BookItem
import com.example.bookstore_android.domain.model.ClientItem
import com.example.bookstore_android.domain.model.ReportFilters
import com.example.bookstore_android.domain.model.ReportPeriod
import com.example.bookstore_android.domain.model.SaleDetail
import com.example.bookstore_android.domain.model.SaleRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias con JUnit del ReportProcessorAdapter (Patrón Adapter).
 */
class ReportProcessorAdapterTest {

    private lateinit var adapter: ReportProcessorAdapter
    private lateinit var sampleRawData: RawReportData

    @Before
    fun setUp() {
        adapter = ReportProcessorAdapter()

        val sales = listOf(
            SaleRecord(idSale = 1L, date = "2026-11-07T01:47:31", total = 14280.0, idClient = 1L, idUser = 1L),
            SaleRecord(idSale = 2L, date = "2026-11-07T01:47:53", total = 14280.0, idClient = 1L, idUser = 1L),
            SaleRecord(idSale = 3L, date = "2026-10-15T10:00:00", total = 12000.0, idClient = 2L, idUser = 1L)
        )

        val details = listOf(
            SaleDetail(idDetail = 1L, idSale = 1L, idBook = 1L, quantity = 1, total = 14280.0),
            SaleDetail(idDetail = 2L, idSale = 2L, idBook = 1L, quantity = 1, total = 14280.0),
            SaleDetail(idDetail = 3L, idSale = 3L, idBook = 2L, quantity = 1, total = 12000.0)
        )

        val books = listOf(
            BookItem(id = 1L, title = "Harry Potter", price = 12000.0),
            BookItem(id = 2L, title = "El Principito", price = 10000.0)
        )

        val clients = listOf(
            ClientItem(idClient = 1L, name = "Stiven Valeriano", idNumber = 1000123L),
            ClientItem(idClient = 2L, name = "Laura Perez", idNumber = 2000456L)
        )

        sampleRawData = RawReportData(sales, details, books, clients)
    }

    @Test
    fun process_withoutDateFilter_returnsAllSalesAndCorrectTotal() {
        val result = adapter.process(sampleRawData, ReportFilters(period = ReportPeriod.DAILY, date = null))

        assertEquals(3, result.sales.size)
        assertEquals(40560.0, result.totalAmount, 0.001)
        assertEquals(2, result.summary.size) // 2026-11-07 y 2026-10-15
    }

    @Test
    fun process_withDailyPeriod_showsOnlySalesOfThatDay() {
        // En diario solo debe mostrar las ventas del día especificado
        val result = adapter.process(
            sampleRawData,
            ReportFilters(period = ReportPeriod.DAILY, date = "2026-11-07")
        )

        assertEquals(2, result.sales.size)
        assertTrue(result.sales.all { it.date.startsWith("2026-11-07") })
        assertEquals(28560.0, result.totalAmount, 0.001)
        assertEquals(1, result.summary.size)
        assertEquals("2026-11-07", result.summary.first().period)
        assertEquals(2, result.summary.first().salesCount)
    }

    @Test
    fun process_withDailyPeriod_whenDateHasNoSales_returnsEmptySales() {
        // En diario con una fecha sin ventas debe retornar 0 ventas y $0
        val result = adapter.process(
            sampleRawData,
            ReportFilters(period = ReportPeriod.DAILY, date = "2026-11-12")
        )

        assertEquals(0, result.sales.size)
        assertEquals(0.0, result.totalAmount, 0.001)
        assertTrue(result.summary.isEmpty())
    }

    @Test
    fun process_withMonthlyPeriod_showsOnlySalesOfThatMonth() {
        // En mensual solo debe mostrar las ventas del mes especificado
        val resultNovember = adapter.process(
            sampleRawData,
            ReportFilters(period = ReportPeriod.MONTHLY, date = "2026-11-12")
        )

        assertEquals(2, resultNovember.sales.size)
        assertTrue(resultNovember.sales.all { it.date.startsWith("2026-11") })
        assertEquals(28560.0, resultNovember.totalAmount, 0.001)
        assertEquals(1, resultNovember.summary.size)
        assertEquals("2026-11", resultNovember.summary.first().period)
        assertEquals(2, resultNovember.summary.first().salesCount)

        // Mes de octubre (debe traer solo la venta de octubre)
        val resultOctober = adapter.process(
            sampleRawData,
            ReportFilters(period = ReportPeriod.MONTHLY, date = "2026-10-15")
        )

        assertEquals(1, resultOctober.sales.size)
        assertTrue(resultOctober.sales.all { it.date.startsWith("2026-10") })
        assertEquals(12000.0, resultOctober.totalAmount, 0.001)
        assertEquals("2026-10", resultOctober.summary.first().period)
    }

    @Test
    fun process_withYearlyPeriod_showsOnlySalesOfThatYear() {
        // En anual solo debe mostrar las ventas del año especificado
        val result2026 = adapter.process(
            sampleRawData,
            ReportFilters(period = ReportPeriod.YEARLY, date = "2026-11-12")
        )

        assertEquals(3, result2026.sales.size)
        assertTrue(result2026.sales.all { it.date.startsWith("2026") })
        assertEquals(40560.0, result2026.totalAmount, 0.001)
        assertEquals(1, result2026.summary.size)
        assertEquals("2026", result2026.summary.first().period)
        assertEquals(3, result2026.summary.first().salesCount)

        // Año 2025 (no tiene ventas registradas)
        val result2025 = adapter.process(
            sampleRawData,
            ReportFilters(period = ReportPeriod.YEARLY, date = "2025-01-01")
        )

        assertEquals(0, result2025.sales.size)
        assertEquals(0.0, result2025.totalAmount, 0.001)
        assertTrue(result2025.summary.isEmpty())
    }

    @Test
    fun process_withClientFilter_returnsOnlyMatchingSales() {
        val result = adapter.process(sampleRawData, ReportFilters(idClient = 1L, date = null))

        assertEquals(2, result.sales.size)
        assertTrue(result.sales.all { it.clientName == "Stiven Valeriano" })
        assertEquals(28560.0, result.totalAmount, 0.001)
    }

    @Test
    fun process_withBookFilter_returnsOnlySalesContainingBook() {
        val result = adapter.process(sampleRawData, ReportFilters(idBook = 2L, date = null))

        assertEquals(1, result.sales.size)
        assertEquals(3L, result.sales.first().idSale)
        assertEquals(12000.0, result.totalAmount, 0.001)
    }
}
