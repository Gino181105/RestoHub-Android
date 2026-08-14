package com.example.apprestaurante.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales_documents",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["orderId"], unique = true),
        Index(value = ["series", "number"], unique = true),
        Index("issuedAt")
    ]
)
data class SalesDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val documentType: String,
    val series: String,
    val number: Int,
    val clientName: String,
    val clientDocument: String,
    val businessName: String,
    val fiscalAddress: String,
    val subtotal: Double,
    val igv: Double,
    val total: Double,
    val paymentMethod: String,
    val issuerUserId: Long,
    val issuedAt: Long = System.currentTimeMillis()
) {
    val formattedNumber: String
        get() = "$series-${number.toString().padStart(8, '0')}"
}
