package com.example.bookstore_android.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.bookstore_android.R
import com.example.bookstore_android.data.adapter.SharedPrefsSessionAdapter
import com.example.bookstore_android.data.adapter.UserNetworkAdapter
import com.example.bookstore_android.data.remote.HttpLoginApiClient
import com.example.bookstore_android.data.repository.LoginRepositoryImpl
import com.example.bookstore_android.presentation.viewmodel.LoginUiState
import com.example.bookstore_android.presentation.viewmodel.LoginViewModel
import com.example.bookstore_android.presentation.viewmodel.LoginViewModelFactory

/**
 * Patrón MVVM - View (Capa de Presentación):
 * Observa el estado expuesto por LoginViewModel y tras el inicio de sesión exitoso
 * conecta directamente con el Dashboard (Panel Principal).
 */
class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels {
        val loginApi = HttpLoginApiClient()
        val userAdapter = UserNetworkAdapter()
        val sessionAdapter = SharedPrefsSessionAdapter({ applicationContext }, userAdapter)
        val repository = LoginRepositoryImpl(loginApi, sessionAdapter, userAdapter)
        LoginViewModelFactory(repository)
    }

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var layoutError: View
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnLogin: Button
    private lateinit var pbLoading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si ya hay sesión activa previa, navegar directo al Dashboard
        if (viewModel.currentState is LoginUiState.Success) {
            navigateToDashboard()
            return
        }

        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        layoutError = findViewById(R.id.layoutError)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
        btnLogin = findViewById(R.id.btnLogin)
        pbLoading = findViewById(R.id.pbLoading)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            viewModel.login(email, password)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is LoginUiState.Initial -> renderInitialState()
                is LoginUiState.Loading -> renderLoadingState()
                is LoginUiState.Success -> navigateToDashboard()
                is LoginUiState.Error -> renderErrorState(state)
            }
        }
    }

    private fun renderInitialState() {
        layoutError.visibility = View.GONE
        btnLogin.isEnabled = true
        btnLogin.text = "Iniciar sesión"
        pbLoading.visibility = View.GONE
    }

    private fun renderLoadingState() {
        layoutError.visibility = View.GONE
        btnLogin.isEnabled = false
        btnLogin.text = "Iniciando sesión..."
        pbLoading.visibility = View.VISIBLE
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun renderErrorState(state: LoginUiState.Error) {
        btnLogin.isEnabled = true
        btnLogin.text = "Iniciar sesión"
        pbLoading.visibility = View.GONE

        tvErrorMessage.text = state.message
        layoutError.visibility = View.VISIBLE
    }
}
