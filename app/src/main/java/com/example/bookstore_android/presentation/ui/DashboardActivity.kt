package com.example.bookstore_android.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.bookstore_android.R
import com.example.bookstore_android.data.adapter.SharedPrefsSessionAdapter
import com.example.bookstore_android.data.adapter.UserNetworkAdapter
import com.example.bookstore_android.data.remote.HttpDashboardApiClient
import com.example.bookstore_android.data.remote.HttpLoginApiClient
import com.example.bookstore_android.data.repository.DashboardRepositoryImpl
import com.example.bookstore_android.data.repository.LoginRepositoryImpl
import com.example.bookstore_android.domain.model.DashboardStats
import com.example.bookstore_android.presentation.viewmodel.DashboardUiState
import com.example.bookstore_android.presentation.viewmodel.DashboardViewModel
import com.example.bookstore_android.presentation.viewmodel.DashboardViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels {
        val userAdapter = UserNetworkAdapter()
        val sessionAdapter = SharedPrefsSessionAdapter({ applicationContext }, userAdapter)
        val loginRepository = LoginRepositoryImpl(HttpLoginApiClient(), sessionAdapter, userAdapter)
        val dashboardRepository = DashboardRepositoryImpl(HttpDashboardApiClient())
        DashboardViewModelFactory(dashboardRepository, loginRepository)
    }

    private lateinit var btnMenuDropdown: View
    private lateinit var tvWelcomeUser: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvError: TextView

    private lateinit var tvTotalBooks: TextView
    private lateinit var tvTotalClients: TextView
    private lateinit var tvSalesToday: TextView
    private lateinit var tvTransactionsToday: TextView
    private lateinit var cardTransactionsToday: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        btnMenuDropdown = findViewById(R.id.btnMenuDropdown)
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser)
        pbLoading = findViewById(R.id.pbDashboardLoading)
        tvError = findViewById(R.id.tvDashboardError)

        tvTotalBooks = findViewById(R.id.tvTotalBooks)
        tvTotalClients = findViewById(R.id.tvTotalClients)
        tvSalesToday = findViewById(R.id.tvSalesToday)
        tvTransactionsToday = findViewById(R.id.tvTransactionsToday)
        cardTransactionsToday = findViewById(R.id.cardTransactionsToday)

        val loggedUser = viewModel.getLoggedUser()
        if (loggedUser != null) {
            val roleName = if (loggedUser.role == "ADMIN") "Administrador" else "Empleado"
            tvWelcomeUser.text = "Bienvenido/a, ${loggedUser.name} · $roleName"
        }
    }

    private fun setupListeners() {
        // Menú desplegable flotante (PopupMenu) con el apartado de Reportes
        btnMenuDropdown.setOnClickListener { anchorView ->
            val popup = PopupMenu(this, anchorView)
            popup.menuInflater.inflate(R.menu.dashboard_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_reports -> {
                        startActivity(Intent(this, ReportsActivity::class.java))
                        true
                    }
                    R.id.action_logout -> {
                        viewModel.logout()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Acceso directo a reportes al tocar la tarjeta de transacciones
        cardTransactionsToday.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is DashboardUiState.Loading -> {
                    pbLoading.visibility = View.VISIBLE
                    tvError.visibility = View.GONE
                }
                is DashboardUiState.Success -> {
                    pbLoading.visibility = View.GONE
                    tvError.visibility = View.GONE
                    renderStats(state.stats)
                }
                is DashboardUiState.Error -> {
                    pbLoading.visibility = View.GONE
                    tvError.visibility = View.VISIBLE
                    tvError.text = state.message
                }
            }
        }
    }

    private fun renderStats(stats: DashboardStats) {
        tvTotalBooks.text = stats.totalBooks.toString()
        tvTotalClients.text = stats.totalClients.toString()
        tvSalesToday.text = formatCurrencyCop(stats.salesToday)
        tvTransactionsToday.text = stats.transactionsToday.toString()
    }

    // Formateo de pesos colombianos (COP) sin decimales idéntico a format.ts
    private fun formatCurrencyCop(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))
        format.maximumFractionDigits = 0
        return format.format(value)
    }
}
