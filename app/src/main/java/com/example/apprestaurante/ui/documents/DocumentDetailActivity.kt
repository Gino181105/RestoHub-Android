package com.example.apprestaurante.ui.documents

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.core.SalesDocumentPdf
import com.example.apprestaurante.core.app
import com.example.apprestaurante.databinding.ActivityDocumentDetailBinding
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.ui.adapters.OrderItemAdapter
import com.example.apprestaurante.ui.common.RestoHubViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDocumentDetailBinding
    private val viewModel: DocumentDetailViewModel by viewModels {
        RestoHubViewModelFactory(app.container)
    }
    private val itemAdapter = OrderItemAdapter()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.adapter = itemAdapter

        val documentId = intent.getLongExtra(EXTRA_DOCUMENT_ID, 0L)
        if (documentId <= 0L) {
            finish()
            return
        }

        binding.btnSharePdf.setOnClickListener {
            val ui = viewModel.state.value
            val document = ui.document ?: return@setOnClickListener
            lifecycleScope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        SalesDocumentPdf.create(this@DocumentDetailActivity, document, ui.items)
                    }
                }.onSuccess { file ->
                    val uri = FileProvider.getUriForFile(
                        this@DocumentDetailActivity,
                        "$packageName.fileprovider",
                        file
                    )
                    startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            },
                            "Compartir comprobante"
                        )
                    )
                }.onFailure {
                    Snackbar.make(binding.root, "No se pudo generar el PDF", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { ui ->
                    binding.progress.visibility = if (ui.loading) View.VISIBLE else View.GONE
                    ui.document?.let { document ->
                        binding.toolbar.title = document.formattedNumber
                        binding.tvType.text = DocumentType.from(document.documentType).label
                        binding.tvNumber.text = document.formattedNumber
                        binding.tvDate.text = "Emitido: ${dateFormat.format(Date(document.issuedAt))}"
                        binding.tvClient.text = "Cliente: ${document.clientName}"
                        binding.tvDocument.text = "Documento: ${document.clientDocument.ifBlank { "Sin documento" }}"
                        binding.tvBusiness.text = if (document.businessName.isBlank()) {
                            ""
                        } else {
                            "Razón social: ${document.businessName}\nDirección: ${document.fiscalAddress}"
                        }
                        binding.tvPayment.text = "Medio de pago: ${document.paymentMethod}"
                        binding.tvSubtotal.text = "Op. gravada: ${PriceFormatter.format(document.subtotal)}"
                        binding.tvIgv.text = "IGV (18%): ${PriceFormatter.format(document.igv)}"
                        binding.tvTotal.text = "Total: ${PriceFormatter.format(document.total)}"
                        itemAdapter.submitList(ui.items)
                    }
                    ui.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
        viewModel.load(documentId)
    }

    companion object {
        const val EXTRA_DOCUMENT_ID = "document_id"
    }
}
