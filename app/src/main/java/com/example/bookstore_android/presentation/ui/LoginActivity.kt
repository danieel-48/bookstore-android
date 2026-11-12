package com.example.bookstore_android.presentation.ui

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
 * Observa el estado expuesto por LoginViewModel y actualiza los componentes visuales.
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

    private lateinit var cardLoginForm: View
    private lateinit var cardLoggedIn: View
    private lateinit var tvLoggedInName: TextView
    private lateinit var tvLoggedInEmail: TextView
    private lateinit var tvLoggedInRole: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        cardLoginForm = findViewById(R.id.cardLoginForm)
        cardLoggedIn = findViewById(R.id.cardLoggedIn)
        tvLoggedInName = findViewById(R.id.tvLoggedInName)
        tvLoggedInEmail = findViewById(R.id.tvLoggedInEmail)
        tvLoggedInRole = findViewById(R.id.tvLoggedInRole)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        btnLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is LoginUiState.Initial -> renderInitialState()
                is LoginUiState.Loading -> renderLoadingState()
                is LoginUiState.Success -> renderSuccessState(state)
                is LoginUiState.Error -> renderErrorState(state)
            }
        }
    }

    private fun renderInitialState() {
        cardLoginForm.visibility = View.VISIBLE
        cardLoggedIn.visibility = View.GONE
        layoutError.visibility = View.GONE
        btnLogin.isEnabled = true
        btnLogin.text = "Iniciar sesión"
        pbLoading.visibility = View.GONE
        etPassword.text?.clear()
    }

    private fun renderLoadingState() {
        layoutError.visibility = View.GONE
        btnLogin.isEnabled = false
        btnLogin.text = "Iniciando sesión..."
        pbLoading.visibility = View.VISIBLE
    }

    private fun renderSuccessState(state: LoginUiState.Success) {
        cardLoginForm.visibility = View.GONE
        cardLoggedIn.visibility = View.VISIBLE
        layoutError.visibility = View.GONE
        btnLogin.isEnabled = true
        btnLogin.text = "Iniciar sesión"
        pbLoading.visibility = View.GONE

        tvLoggedInName.text = "Nombre: ${state.user.name}"
        tvLoggedInEmail.text = "Correo: ${state.user.email}"
        tvLoggedInRole.text = "ROL: ${state.user.role}"
    }

    private fun renderErrorState(state: LoginUiState.Error) {
        cardLoginForm.visibility = View.VISIBLE
        cardLoggedIn.visibility = View.GONE
        btnLogin.isEnabled = true
        btnLogin.text = "Iniciar sesión"
        pbLoading.visibility = View.GONE

        tvErrorMessage.text = state.message
        layoutError.visibility = View.VISIBLE
    }
}
