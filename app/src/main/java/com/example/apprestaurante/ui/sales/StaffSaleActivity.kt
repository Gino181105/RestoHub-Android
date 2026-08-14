package com.example.apprestaurante.ui.sales

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprestaurante.AppContainer
import com.example.apprestaurante.R
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.core.app
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.databinding.ActivityStaffSaleBinding
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.domain.model.ServiceType
import com.example.apprestaurante.ui.adapters.ProductAdapter
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.example.apprestaurante.ui.orders.OrderDetailActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class StaffSaleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStaffSaleBinding
    private val viewModel: StaffSaleViewModel by viewModels {
        RestoHubViewModelFactory(app.container)
    }
    private val productAdapter = ProductAdapter(viewModel::add)
    private var clients: List<UserEntity> = emptyList()
    private var selectedService = ServiceType.TABLE
    private var selectedDocument = DocumentType.BOLETA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!app.container.session.role.isStaff) {
            finish()
            return
        }
        binding = ActivityStaffSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = productAdapter
        binding.rvProducts.isNestedScrollingEnabled = false

        configureServiceSpinner()
        configureDocumentSpinner()
        binding.btnClear.setOnClickListener { viewModel.clearSelection() }
        binding.btnRemoveOne.setOnClickListener { showRemoveDialog() }
        binding.btnCreateSale.setOnClickListener { submit() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { ui ->
                        productAdapter.submitList(ui.products)
                        binding.progress.visibility = if (ui.processing) View.VISIBLE else View.GONE
                        binding.btnCreateSale.isEnabled = !ui.processing && ui.selected.isNotEmpty()
                        binding.tvTotal.text = "Total: ${PriceFormatter.format(ui.total)}"
                        binding.tvSelection.text = if (ui.selected.isEmpty()) {
                            "No hay productos seleccionados"
                        } else {
                            ui.selected.joinToString("\n") {
                                "${it.quantity} × ${it.product.name} = ${PriceFormatter.format(it.product.price * it.quantity)}"
                            }
                        }
                        binding.btnClear.isEnabled = ui.selected.isNotEmpty()
                        binding.btnRemoveOne.isEnabled = ui.selected.isNotEmpty()
                        if (clients.map { it.id } != ui.clients.map { it.id }) {
                            clients = ui.clients
                            binding.spClient.adapter = ArrayAdapter(
                                this@StaffSaleActivity,
                                android.R.layout.simple_spinner_dropdown_item,
                                clients.map { it.fullName }
                            )
                            val walkInIndex = clients.indexOfFirst { it.email == AppContainer.WALK_IN_EMAIL }
                            if (walkInIndex >= 0) binding.spClient.setSelection(walkInIndex)
                        }
                    }
                }
                launch {
                    viewModel.message.collect { message ->
                        message?.let {
                            if (it.startsWith("SALE_OK:")) {
                                val orderId = it.split(":").getOrNull(1)?.toLongOrNull()
                                Snackbar.make(binding.root, "Venta registrada correctamente", Snackbar.LENGTH_LONG).show()
                                if (orderId != null) {
                                    binding.root.postDelayed({
                                        startActivity(
                                            Intent(this@StaffSaleActivity, OrderDetailActivity::class.java)
                                                .putExtra(OrderDetailActivity.EXTRA_ORDER_ID, orderId)
                                        )
                                        finish()
                                    }, 500)
                                }
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
            this,
            android.R.layout.simple_spinner_dropdown_item,
            ServiceType.entries.map { it.label }
        )
        binding.spService.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedService = ServiceType.entries[position]
                binding.tilTableNumber.visibility = if (selectedService == ServiceType.TABLE) View.VISIBLE else View.GONE
                binding.tilDeliveryAddress.visibility = if (selectedService == ServiceType.DELIVERY) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun configureDocumentSpinner() {
        binding.spDocumentType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            DocumentType.entries.map { it.label }
        )
        binding.spDocumentType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDocument = DocumentType.entries[position]
                val invoice = selectedDocument == DocumentType.FACTURA
                binding.tilCustomerDocument.hint = if (invoice) "RUC (11 dígitos)" else "DNI (opcional)"
                binding.tilBusinessName.visibility = if (invoice) View.VISIBLE else View.GONE
                binding.tilFiscalAddress.visibility = if (invoice) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.spClient.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                clients.getOrNull(position)?.let { client ->
                    binding.etCustomerDocument.setText(client.documentNumber)
                    binding.etBusinessName.setText(client.businessName)
                    binding.etFiscalAddress.setText(client.address)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun showRemoveDialog() {
        val selected = viewModel.state.value.selected
        MaterialAlertDialogBuilder(this)
            .setTitle("Quitar una unidad")
            .setItems(selected.map { "${it.quantity} × ${it.product.name}" }.toTypedArray()) { _, index ->
                selected.getOrNull(index)?.let { viewModel.removeOne(it.product.id) }
            }
            .show()
    }

    private fun submit() {
        val client = clients.getOrNull(binding.spClient.selectedItemPosition)
        if (client == null) {
            Snackbar.make(binding.root, "Selecciona un cliente", Snackbar.LENGTH_LONG).show()
            return
        }
        val payment = when (binding.rgPayment.checkedRadioButtonId) {
            R.id.rbCard -> "Tarjeta"
            R.id.rbYape -> "Yape / Plin"
            else -> "Efectivo"
        }
        viewModel.createSale(
            clientId = client.id,
            paymentMethod = payment,
            serviceType = selectedService,
            notes = binding.etNotes.text?.toString().orEmpty(),
            tableNumber = binding.etTableNumber.text?.toString().orEmpty(),
            deliveryAddress = binding.etDeliveryAddress.text?.toString().orEmpty(),
            documentType = selectedDocument,
            customerDocument = binding.etCustomerDocument.text?.toString().orEmpty(),
            businessName = binding.etBusinessName.text?.toString().orEmpty(),
            fiscalAddress = binding.etFiscalAddress.text?.toString().orEmpty(),
            payNow = binding.cbPayNow.isChecked
        )
    }
}
