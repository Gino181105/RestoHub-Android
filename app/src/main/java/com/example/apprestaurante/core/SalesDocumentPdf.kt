package com.example.apprestaurante.core

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.apprestaurante.data.local.entity.OrderItemEntity
import com.example.apprestaurante.data.local.entity.SalesDocumentEntity
import com.example.apprestaurante.domain.model.DocumentType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SalesDocumentPdf {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 42f

    fun create(
        context: Context,
        document: SalesDocumentEntity,
        items: List<OrderItemEntity>
    ): File {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE"))
        var y = 52f

        fun text(value: String, size: Float = 11f, bold: Boolean = false, x: Float = MARGIN) {
            paint.textSize = size
            paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            paint.color = android.graphics.Color.BLACK
            canvas.drawText(value, x, y, paint)
            y += size + 7f
        }

        fun line() {
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
            y += 12f
        }

        text("RESTOHUB", 22f, bold = true)
        text("Restaurante académico - Lima, Perú", 10f)
        text("RUC: 20123456789", 10f)
        y += 8f

        val type = DocumentType.from(document.documentType)
        text(type.label.uppercase(), 16f, bold = true)
        text(document.formattedNumber, 14f, bold = true)
        text("Fecha de emisión: ${dateFormat.format(Date(document.issuedAt))}")
        text("Pedido: #${document.orderId}")
        line()

        text("Cliente: ${document.clientName}", bold = true)
        text("Documento: ${document.clientDocument.ifBlank { "Sin documento" }}")
        if (document.businessName.isNotBlank()) text("Razón social: ${document.businessName}")
        if (document.fiscalAddress.isNotBlank()) text("Dirección fiscal: ${document.fiscalAddress}")
        text("Medio de pago: ${document.paymentMethod}")
        line()

        text("CANT.  DESCRIPCIÓN                         P. UNIT.      IMPORTE", 10f, bold = true)
        items.take(18).forEach { item ->
            val shortName = item.productName.take(30).padEnd(30, ' ')
            val row = "${item.quantity.toString().padEnd(6)} ${shortName} ${PriceFormatter.format(item.unitPrice).padStart(11)} ${PriceFormatter.format(item.unitPrice * item.quantity).padStart(12)}"
            text(row, 9f)
        }
        if (items.size > 18) text("... ${items.size - 18} producto(s) adicionales", 9f)
        line()

        text("Op. gravada: ${PriceFormatter.format(document.subtotal)}", 12f, x = 330f)
        text("IGV (18%): ${PriceFormatter.format(document.igv)}", 12f, x = 330f)
        text("TOTAL: ${PriceFormatter.format(document.total)}", 14f, bold = true, x = 330f)
        y += 25f
        text("Gracias por su preferencia", 12f, bold = true)
        text("Representación interna para fines académicos.", 9f)
        text("No constituye comprobante electrónico enviado a SUNAT.", 9f)

        pdf.finishPage(page)
        val directory = File(context.cacheDir, "comprobantes").apply { mkdirs() }
        val output = File(directory, "${document.formattedNumber}.pdf")
        FileOutputStream(output).use(pdf::writeTo)
        pdf.close()
        return output
    }
}
