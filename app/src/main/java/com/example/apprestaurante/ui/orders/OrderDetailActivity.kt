package com.example.apprestaurante.ui.orders

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.ActivityOrderDetailBinding
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.domain.model.OrderStatus
import com.example.apprestaurante.domain.model.PaymentStatus
import com.example.apprestaurante.domain.model.UserRole
import com.example.apprestaurante.ui.adapters.OrderItemAdapter
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.example.apprestaurante.ui.documents.DocumentDetailActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailBinding
    private val viewModel: OrderDetailViewModel by viewModels {
        RestoHubViewModelFactory(app.container)
    }
    private val itemAdapter = OrderItemAdapter()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE"))
    private var statusValues: List<OrderStatus> = emptyList()
    private var currentDocumentId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.adapter = itemAdapter

        val orderId = intent.getLongExtra(EXTRA_ORDER_ID, 0L)
        if (orderId <= 0L) {
            finish()
            return
        }

        binding.btnCancel.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Cancelar pedido")
                .setMessage("Se devolverá el stock de los productos. ¿Continuar?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí, cancelar") { _, _ -> viewModel.cancelClient() }
                .show()
        }
        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Borrar pedido")
                .setMessage("Solo se puede borrar un pedido cancelado sin comprobante")
                .setNegativeButton("No", null)
                .setPositiveButton("Borrar") { _, _ -> viewModel.delete() }
                .show()
        }
        binding.btnUpdateStatus.setOnClickListener {
            statusValues.getOrNull(binding.spNextStatus.selectedItemPosition)?.let(viewModel::updateStaff)
        }
        binding.btnPay.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Registrar pago")
                .setMessage("Se marcará como pagado y se generará la boleta o factura solicitada")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Cobrar y emitir") { _, _ -> viewModel.markPaid() }
                .show()
        }
        binding.btnViewDocument.setOnClickListener {
            currentDocumentId?.let(::openDocument)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { ui ->
                    binding.progress.visibility = if (ui.loading) View.VISIBLE else View.GONE
                    ui.order?.let { order ->
                        val status = OrderStatus.from(order.status)
                        val paymentStatus = PaymentStatus.from(order.paymentStatus)
                        val isStaff = app.container.session.role.isStaff
                        binding.toolbar.title = "Pedido #${order.id}"
                        binding.tvClient.text = ui.client?.let {
                            "Cliente: ${it.fullName}\n${it.email}"
                        } ?: "Cliente #${order.userId}"
                        binding.tvStatus.text = "Estado: ${status.label}"
                        binding.tvDate.text = "Fecha: ${dateFormat.format(Date(order.createdAt))}"
                        binding.tvService.text = buildString {
                            append("Servicio: ${order.serviceType}")
                            if (order.tableNumber.isNotBlank()) append(" · Mesa ${order.tableNumber}")
                            if (order.deliveryAddress.isNotBlank()) append("\nDirección: ${order.deliveryAddress}")
                        }
                        binding.tvPayment.text = "Pago: ${order.paymentMethod} · ${paymentStatus.label}"
                        binding.tvDocumentRequest.text = buildString {
                            append("Comprobante solicitado: ${DocumentType.from(order.documentType).label}")
                            if (order.customerDocument.isNotBlank()) append(" · ${order.customerDocument}")
                            if (order.businessName.isNotBlank()) append("\n${order.businessName}")
                        }
                        binding.tvNotes.text = if (order.notes.isBlank()) {
                            "Notas: Sin observaciones"
                        } else {
                            "Notas: ${order.notes}"
                        }
                        binding.tvTotal.text = "Total: ${PriceFormatter.format(order.total)}"
                        itemAdapter.submitList(ui.items)

                        binding.staffControls.visibility = if (isStaff) View.VISIBLE else View.GONE
                        binding.btnCancel.visibility =
                            if (!isStaff && status.canClientCancel() && paymentStatus == PaymentStatus.PENDING) View.VISIBLE else View.GONE
                        binding.btnDelete.visibility = if (status.canDelete()) View.VISIBLE else View.GONE
                        binding.btnPay.visibility =
                            if (isStaff && paymentStatus == PaymentStatus.PENDING && status != OrderStatus.CANCELLED) View.VISIBLE else View.GONE

                        if (isStaff) {
                            statusValues = status.nextForReceptionist()
                            binding.spNextStatus.adapter = ArrayAdapter(
                                this@OrderDetailActivity,
                                android.R.layout.simple_spinner_dropdown_item,
                                statusValues.map { it.label }
                            )
                            binding.btnUpdateStatus.isEnabled = statusValues.isNotEmpty()
                            binding.tvNoActions.visibility = if (statusValues.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }

                    currentDocumentId = ui.document?.id
                    binding.btnViewDocument.visibility = if (ui.document != null) View.VISIBLE else View.GONE
                    binding.tvIssuedDocument.text = ui.document?.let {
                        "Comprobante emitido: ${it.formattedNumber}"
                    }.orEmpty()
                    binding.tvIssuedDocument.visibility = if (ui.document != null) View.VISIBLE else View.GONE

                    ui.message?.let { message ->
                        if (message.startsWith("DOCUMENT_OK:")) {
                            val id = message.substringAfter(":").toLongOrNull()
                            Snackbar.make(binding.root, "Pago registrado y comprobante emitido", Snackbar.LENGTH_LONG).show()
                            if (id != null) binding.root.postDelayed({ openDocument(id) }, 600)
                        } else {
                            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                        }
                        viewModel.consumeMessage()
                    }
                    if (ui.close) binding.root.postDelayed({ finish() }, 500)
                }
            }
        }
        viewModel.load(orderId)
    }

    private fun openDocument(documentId: Long) {
        startActivity(
            Intent(this, DocumentDetailActivity::class.java)
                .putExtra(DocumentDetailActivity.EXTRA_DOCUMENT_ID, documentId)
        )
    }

    companion object {
        const val EXTRA_ORDER_ID = "order_id"
    }
}
