package com.example.apprestaurante.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apprestaurante.R
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.data.local.model.OrderSummary
import com.example.apprestaurante.databinding.ItemOrderBinding
import com.example.apprestaurante.domain.model.DocumentType
import com.example.apprestaurante.domain.model.OrderStatus
import com.example.apprestaurante.domain.model.PaymentStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val showClient: Boolean,
    private val onClick: (OrderSummary) -> Unit
) : ListAdapter<OrderSummary, OrderAdapter.ViewHolder>(DiffCallback) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderSummary) {
            val status = OrderStatus.from(item.status)
            val payment = PaymentStatus.from(item.paymentStatus)
            binding.tvOrderId.text = "Pedido #${item.id}"
            binding.tvClient.text = if (showClient) {
                "${item.clientName} · ${item.clientEmail}"
            } else {
                "${item.itemCount} producto(s)"
            }
            binding.tvClient.visibility = View.VISIBLE
            binding.tvStatus.text = status.label
            binding.tvStatus.setTextColor(
                binding.root.context.getColor(
                    when (status) {
                        OrderStatus.CANCELLED -> R.color.danger
                        OrderStatus.DELIVERED -> R.color.success
                        OrderStatus.READY -> R.color.primary
                        else -> R.color.warning
                    }
                )
            )
            binding.tvDate.text = dateFormat.format(Date(item.createdAt))
            binding.tvDetails.text = buildString {
                append("${item.serviceType} · ${item.paymentMethod}")
                append(" · ${payment.label}")
                append("\n${DocumentType.from(item.documentType).label}")
                if (item.tableNumber.isNotBlank()) append(" · Mesa ${item.tableNumber}")
            }
            binding.tvTotal.text = PriceFormatter.format(item.total)
            binding.root.setOnClickListener { onClick(item) }
            binding.btnOpen.setOnClickListener { onClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<OrderSummary>() {
        override fun areItemsTheSame(oldItem: OrderSummary, newItem: OrderSummary) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OrderSummary, newItem: OrderSummary) =
            oldItem == newItem
    }
}
