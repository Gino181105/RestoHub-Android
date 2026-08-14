package com.example.apprestaurante.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.apprestaurante.core.Validation
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.ActivityRegisterBinding
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.example.apprestaurante.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels {
        RestoHubViewModelFactory(app.container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnRegister.setOnClickListener { submit() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
                    binding.btnRegister.isEnabled = !state.loading
                    if (state.success) {
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finishAffinity()
                    }
                    state.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.consumeMessage()
                    }
                }
            }
        }
    }

    private fun submit() {
        val name = binding.etName.text?.toString().orEmpty()
        val email = binding.etEmail.text?.toString().orEmpty()
        val phone = binding.etPhone.text?.toString().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirm.text?.toString().orEmpty()

        binding.tilName.error = Validation.name(name)
        binding.tilEmail.error = Validation.email(email)
        binding.tilPassword.error = Validation.password(password)
        binding.tilConfirm.error = if (password != confirm) "Las contraseñas no coinciden" else null

        if (
            binding.tilName.error == null && binding.tilEmail.error == null &&
            binding.tilPassword.error == null && binding.tilConfirm.error == null
        ) {
            viewModel.register(name, email, phone, password)
        }
    }
}
