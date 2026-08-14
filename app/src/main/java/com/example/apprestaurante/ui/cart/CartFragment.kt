package com.example.apprestaurante.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprestaurante.R
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.core.app
import com.example.apprestaurante.data.local.model.CartProductItem
import com.example.apprestaurante.databinding.FragmentCartBinding
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.domain.model.ServiceType
import com.example.apprestaurante.ui.adapters.CartAdapter
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.example.apprestaurante.ui.main.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CartViewModel by viewModels {
        RestoHubViewModelFactory(requireContext().app.container)
    }
    private lateinit var cartAdapter: CartAdapter
    private var selectedService = ServiceType.TABLE
    private var selectedDocument = DocumentType.BOLETA

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        cartAdapter = CartAdapter(
            onIncrease = viewModel::increase,
            onDecrease = viewModel::decrease,
            onRemove = ::confirmRemove
        )
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter
        binding.rvCart.isNestedScrollingEnabled = false

        configureServiceSpinner()
        configureDocumentSpinner()

        binding.btnCheckout.setOnClickListener {
            clearErrors()
            val payment = when (binding.rgPayment.checkedRadioButtonId) {
                R.id.rbCard -> "Tarjeta"
                R.id.rbYape -> "Yape / Plin"
                else -> "Efectivo"
            }
            viewModel.checkout(
                payment = payment,
                service = selectedService,
                notes = binding.etNotes.text?.toString().orEmpty(),
                tableNumber = binding.etTableNumber.text?.toString().orEmpty(),
                deliveryAddress = binding.etDeliveryAddress.text?.toString().orEmpty(),
                documentType = selectedDocument,
                customerDocument = binding.etCustomerDocument.text?.toString().orEmpty(),
                businessName = binding.etBusinessName.text?.toString().orEmpty(),
                fiscalAddress = binding.etFiscalAddress.text?.toString().orEmpty()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { ui ->
                        cartAdapter.submitList(ui.items)
                        binding.tvTotal.text = "Total: ${PriceFormatter.format(ui.total)}"
                        binding.tvEmpty.visibility = if (ui.items.isEmpty()) View.VISIBLE else View.GONE
                        binding.checkoutCard.visibility = if (ui.items.isEmpty()) View.GONE else View.VISIBLE
                        binding.btnCheckout.isEnabled = !ui.processing && ui.items.isNotEmpty()
                        binding.progress.visibility = if (ui.processing) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.message.collect { message ->
                        message?.let {
                            if (it.startsWith("PEDIDO_OK:")) {
                                val id = it.substringAfter(":")
                                Snackbar.make(
                                    binding.root,
                                    "Pedido #$id registrado. Paga en recepción para emitir tu comprobante",
                                    Snackbar.LENGTH_LONG
                                ).show()
                                clearForm()
                                (activity as? MainActivity)?.selectTab(R.id.nav_client_orders)
                            } else {
                                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                            }
                            viewModel.consumeMessage()
                        }
                    }
                }
            }
        }
    }

    private fun configureServiceSpinner() {
        binding.spService.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            ServiceType.entries.map { it.label }
        )
        binding.spService.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedService = ServiceType.entries[position]
                binding.tilTableNumber.visibility =
                    if (selectedService == ServiceType.TABLE) View.VISIBLE else View.GONE
                binding.tilDeliveryAddress.visibility =
                    if (selectedService == ServiceType.DELIVERY) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun configureDocumentSpinner() {
        binding.spDocumentType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            DocumentType.entries.map { it.label }
        )
        binding.spDocumentType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDocument = DocumentType.entries[position]
                val isInvoice = selectedDocument == DocumentType.FACTURA
                binding.tilCustomerDocument.hint = if (isInvoice) "RUC (11 dígitos)" else "DNI (opcional)"
                binding.tilBusinessName.visibility = if (isInvoice) View.VISIBLE else View.GONE
                binding.tilFiscalAddress.visibility = if (isInvoice) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun clearErrors() {
        binding.tilTableNumber.error = null
        binding.tilDeliveryAddress.error = null
        binding.tilCustomerDocument.error = null
        binding.tilBusinessName.error = null
        binding.tilFiscalAddress.error = null
    }

    private fun clearForm() {
        binding.etNotes.setText("")
        binding.etTableNumber.setText("")
        binding.etDeliveryAddress.setText("")
        binding.etCustomerDocument.setText("")
        binding.etBusinessName.setText("")
        binding.etFiscalAddress.setText("")
    }

    private fun confirmRemove(item: CartProductItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Quitar producto")
            .setMessage("¿Deseas quitar ${item.name} del carrito?")
            .setNegativeButton("No", null)
            .setPositiveButton("Sí, quitar") { _, _ -> viewModel.remove(item) }
            .show()
    }

    override fun onDestroyView() {
        if (::cartAdapter.isInitialized) binding.rvCart.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
