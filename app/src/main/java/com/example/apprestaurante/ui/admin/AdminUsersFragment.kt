package com.example.apprestaurante.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.FragmentAdminUsersBinding
import com.example.apprestaurante.domain.model.UserRole
import com.example.apprestaurante.ui.adapters.UserAdapter
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdminUsersFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminUsersViewModel by viewModels {
        RestoHubViewModelFactory(requireContext().app.container)
    }
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = UserAdapter(
            onEdit = { openForm(it.id) },
            onToggle = { user ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(if (user.isActive) "Desactivar usuario" else "Activar usuario")
                    .setMessage("¿Deseas cambiar el estado de ${user.fullName}?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Confirmar") { _, _ -> viewModel.toggle(user) }
                    .show()
            }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter
        binding.etSearch.doAfterTextChanged { viewModel.setQuery(it?.toString().orEmpty()) }

        val labels = listOf("Todos") + UserRole.entries.map { it.label }
        val values = listOf("TODOS") + UserRole.entries.map { it.name }
        binding.spRole.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            labels
        )
        binding.spRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setRole(values[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.fabAdd.setOnClickListener { openForm(0L) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.users.collect { users ->
                        adapter.submitList(users)
                        binding.tvEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.message.collect { message ->
                        message?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            viewModel.consumeMessage()
                        }
                    }
                }
            }
        }
    }

    private fun openForm(id: Long) {
        startActivity(
            Intent(requireContext(), UserFormActivity::class.java)
                .putExtra(UserFormActivity.EXTRA_USER_ID, id)
        )
    }

    override fun onDestroyView() {
        if (::adapter.isInitialized) binding.rvUsers.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
