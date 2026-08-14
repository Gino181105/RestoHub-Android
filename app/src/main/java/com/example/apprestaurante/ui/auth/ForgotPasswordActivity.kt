package com.example.apprestaurante.ui.auth

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.apprestaurante.core.Validation
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.ActivityForgotPasswordBinding
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels {
        RestoHubViewModelFactory(app.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnReset.setOnClickListener { submit() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
                    binding.btnReset.isEnabled = !state.loading
                    state.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.consumeMessage()
                    }
                    if (state.success) binding.btnReset.postDelayed({ finish() }, 800)
                }
            }
        }
    }

    private fun submit() {
        val email = binding.etEmail.text?.toString().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirm.text?.toString().orEmpty()
        binding.tilEmail.error = Validation.email(email)
        binding.tilPassword.error = Validation.password(password)
        binding.tilConfirm.error = if (password != confirm) "Las contraseñas no coinciden" else null
        if (
            binding.tilEmail.error == null && binding.tilPassword.error == null &&
            binding.tilConfirm.error == null
        ) {
            viewModel.resetPassword(email, password)
        }
    }
}
