package com.example.apprestaurante.ui.reception

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.FragmentReceptionProductsBinding
import com.example.apprestaurante.ui.adapters.ReceptionProductAdapter
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ReceptionProductsFragment : Fragment() {
    private var _binding: FragmentReceptionProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReceptionProductsViewModel by viewModels {
        RestoHubViewModelFactory(requireContext().app.container)
    }
    private lateinit var productAdapter: ReceptionProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FragmentReceptionProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        productAdapter = ReceptionProductAdapter(
            onEdit = { product -> openForm(product.id) },
            onToggle = { product ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(if (product.isActive) "Desactivar producto" else "Activar producto")
                    .setMessage(product.name)
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Aceptar") { _, _ -> viewModel.toggle(product) }
                    .show()
            }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = productAdapter
        binding.fabAdd.setOnClickListener { openForm(0L) }
        binding.cbInactive.isChecked = true
        binding.cbInactive.setOnCheckedChangeListener { _, checked ->
            viewModel.setIncludeInactive(checked)
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setQuery(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.products.collect { products ->
                        productAdapter.submitList(products)
                        binding.tvEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
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

    private fun openForm(productId: Long) {
        startActivity(
            Intent(requireContext(), ProductFormActivity::class.java)
                .putExtra(ProductFormActivity.EXTRA_PRODUCT_ID, productId)
        )
    }

    override fun onDestroyView() {
        if (::productAdapter.isInitialized) binding.rvProducts.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
