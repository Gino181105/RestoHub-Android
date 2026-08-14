package com.example.apprestaurante.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.apprestaurante.core.Validation
import com.example.apprestaurante.core.app
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.databinding.ActivityUserFormBinding
import com.example.apprestaurante.domain.model.UserRole
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class UserFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserFormBinding
    private val viewModel: UserFormViewModel by viewModels {
        RestoHubViewModelFactory(app.container)
    }
    private var currentUser: UserEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.spRole.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            UserRole.entries.map { it.label }
        )
        binding.spDocumentType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("DNI", "RUC", "CE")
        )

        binding.btnSave.setOnClickListener { save() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { ui ->
                    binding.progress.visibility = if (ui.loading) View.VISIBLE else View.GONE
                    binding.btnSave.isEnabled = !ui.loading
                    ui.user?.let { user ->
                        if (currentUser?.id != user.id) {
                            currentUser = user
                            fillForm(user)
                        }
                    }
                    ui.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        viewModel.consumeMessage()
                    }
                    if (ui.saved) binding.root.postDelayed({ finish() }, 500)
                }
            }
        }

        val userId = intent.getLongExtra(EXTRA_USER_ID, 0L)
        binding.toolbar.title = if (userId > 0L) "Editar usuario" else "Nuevo usuario"
        viewModel.load(userId)
    }

    private fun fillForm(user: UserEntity) {
        binding.etName.setText(user.fullName)
        binding.etEmail.setText(user.email)
        binding.etPhone.setText(user.phone)
        binding.etDocumentNumber.setText(user.documentNumber)
        binding.etBusinessName.setText(user.businessName)
        binding.etAddress.setText(user.address)
        binding.spRole.setSelection(UserRole.entries.indexOfFirst { it.name == user.role }.coerceAtLeast(0))
        binding.spDocumentType.setSelection(
            listOf("DNI", "RUC", "CE").indexOf(user.documentType).coerceAtLeast(0)
        )
        binding.swActive.isChecked = user.isActive
        binding.tilPassword.hint = "Nueva contraseña (dejar vacío para conservar)"
    }

    private fun save() {
        val name = binding.etName.text?.toString().orEmpty().trim()
        val email = binding.etEmail.text?.toString().orEmpty().trim()
        val password = binding.etPassword.text?.toString().orEmpty()
        binding.tilName.error = Validation.name(name)
        binding.tilEmail.error = Validation.email(email)
        if (currentUser == null && password.length < 6) {
            binding.tilPassword.error = "Ingresa una contraseña de al menos 6 caracteres"
        } else if (password.isNotBlank() && password.length < 6) {
            binding.tilPassword.error = "Usa al menos 6 caracteres"
        } else {
            binding.tilPassword.error = null
        }
        if (binding.tilName.error != null || binding.tilEmail.error != null || binding.tilPassword.error != null) {
            return
        }

        val role = UserRole.entries[binding.spRole.selectedItemPosition]
        val documentType = binding.spDocumentType.selectedItem.toString()
        val base = currentUser ?: UserEntity(
            fullName = name,
            email = email,
            passwordHash = "",
            role = role.name
        )
        viewModel.save(
            base.copy(
                fullName = name,
                email = email,
                phone = binding.etPhone.text?.toString().orEmpty().trim(),
                documentType = documentType,
                documentNumber = binding.etDocumentNumber.text?.toString().orEmpty().trim(),
                businessName = binding.etBusinessName.text?.toString().orEmpty().trim(),
                address = binding.etAddress.text?.toString().orEmpty().trim(),
                role = role.name,
                isActive = binding.swActive.isChecked
            ),
            password
        )
    }

    companion object {
        const val EXTRA_USER_ID = "user_id"
    }
}
