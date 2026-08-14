package com.example.apprestaurante.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.domain.model.OrderStatus
import com.example.apprestaurante.domain.model.PaymentStatus

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("userId"), Index("status"), Index("createdAt")]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val createdByUserId: Long = userId,
    val total: Double,
    val paymentMethod: String,
    val serviceType: String,
    val notes: String,
    val tableNumber: String = "",
    val deliveryAddress: String = "",
    val documentType: String = DocumentType.BOLETA.name,
    val customerDocument: String = "",
    val businessName: String = "",
    val fiscalAddress: String = "",
    val paymentStatus: String = PaymentStatus.PENDING.name,
    val paidAt: Long? = null,
    val status: String = OrderStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
