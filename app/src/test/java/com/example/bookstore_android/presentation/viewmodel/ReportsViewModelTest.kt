package com.example.bookstore_android.presentation.viewmodel

import com.example.bookstore_android.domain.model.BookItem
import com.example.bookstore_android.domain.model.ClientItem
import com.example.bookstore_android.domain.model.ReportFilters
import com.example.bookstore_android.domain.model.ReportPeriod
import com.example.bookstore_android.domain.model.ReportResult
import com.example.bookstore_android.domain.model.ReportSummary
import com.example.bookstore_android.domain.model.Sale
import com.example.bookstore_android.domain.repository.ReportsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias con JUnit de ReportsViewModel (Patrón MVVM).
 */
class ReportsViewModelTest {

    private lateinit var fakeRepository: FakeReportsRepository
    private lateinit var viewModel: ReportsViewModel

    private val sampleClients = listOf(ClientItem(1L, "Stiven Valeriano", 1000123L))
    private val sampleBooks = listOf(BookItem(1L, "Harry Potter", 12000.0))
    private val sampleSales = listOf(
        Sale(
            idSale = 1L,
            date = "2026-11-07T01:47:31",
            total = 14280.0,
            clientName = "Stiven Valeriano",
            userName = "Vendedor #1",
            items = emptyList()
        )
    )

    private val sampleResult = ReportResult(
        summary = listOf(ReportSummary("2026-11-07", 1, 14280.0)),
        sales = sampleSales,
        totalAmount = 14280.0
    )

    @Before
    fun setUp() {
        fakeRepository = FakeReportsRepository(
            optionsToReturn = Pair(sampleClients, sampleBooks),
            reportResultToReturn = sampleResult
        )
        viewModel = ReportsViewModel(fakeRepository)
    }

    @Test
    fun loadData_onInit_emitsSuccessStateWithData() {
        assertTrue(viewModel.currentState is ReportsUiState.Success)
        val success = viewModel.currentState as ReportsUiState.Success
        assertEquals(1, success.result.sales.size)
        assertEquals(14280.0, success.result.totalAmount, 0.001)
        assertEquals(1, success.clients.size)
        assertEquals(1, success.books.size)
    }

    @Test
    fun setPeriod_updatesPeriodFilterAndEmitsNewState() {
        viewModel.setPeriod(ReportPeriod.MONTHLY)

        assertEquals(ReportPeriod.MONTHLY, viewModel.currentFilters.period)
        assertTrue(viewModel.currentState is ReportsUiState.Success)
    }

    @Test
    fun setClientFilter_updatesClientFilter() {
        viewModel.setClientFilter(1L)

        assertEquals(1L, viewModel.currentFilters.idClient)
        assertTrue(viewModel.currentState is ReportsUiState.Success)
    }

    @Test
    fun setBookFilter_updatesBookFilter() {
        viewModel.setBookFilter(1L)

        assertEquals(1L, viewModel.currentFilters.idBook)
        assertTrue(viewModel.currentState is ReportsUiState.Success)
    }

    @Test
    fun setDateFilter_updatesDateFilter() {
        viewModel.setDateFilter("2026-11-07")

        assertEquals("2026-11-07", viewModel.currentFilters.date)
        assertTrue(viewModel.currentState is ReportsUiState.Success)
    }

    @Test
    fun clearFilters_resetsClientAndBookFiltersAndRestoresTodayDate() {
        viewModel.setDateFilter("2020-01-01")
        viewModel.setClientFilter(1L)
        viewModel.setBookFilter(1L)
        assertEquals(1L, viewModel.currentFilters.idClient)
        assertEquals("2020-01-01", viewModel.currentFilters.date)

        viewModel.clearFilters()

        assertNull(viewModel.currentFilters.idClient)
        assertNull(viewModel.currentFilters.idBook)
        assertEquals(com.example.bookstore_android.domain.model.getTodayLocalDate(), viewModel.currentFilters.date)
    }

    @Test
    fun loadReports_whenRepositoryFails_emitsErrorState() {
        fakeRepository.errorToThrow = Exception("Error al cargar reportes")
        viewModel.loadData()

        assertTrue(viewModel.currentState is ReportsUiState.Error)
        val errorState = viewModel.currentState as ReportsUiState.Error
        assertEquals("Error al cargar reportes", errorState.message)
    }

    private class FakeReportsRepository(
        var optionsToReturn: Pair<List<ClientItem>, List<BookItem>>? = null,
        var reportResultToReturn: ReportResult? = null,
        var errorToThrow: Exception? = null
    ) : ReportsRepository {

        override fun getReports(filters: ReportFilters, callback: (Result<ReportResult>) -> Unit) {
            val error = errorToThrow
            val result = reportResultToReturn
            if (error != null) {
                callback(Result.failure(error))
            } else if (result != null) {
                callback(Result.success(result))
            }
        }

        override fun getFilterOptions(callback: (Result<Pair<List<ClientItem>, List<BookItem>>>) -> Unit) {
            val options = optionsToReturn
            if (options != null) {
                callback(Result.success(options))
            } else {
                callback(Result.failure(Exception("No options")))
            }
        }
    }
}
