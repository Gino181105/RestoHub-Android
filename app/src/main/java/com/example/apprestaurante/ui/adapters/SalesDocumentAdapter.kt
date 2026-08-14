package com.example.apprestaurante.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.data.local.entity.SalesDocumentEntity
import com.example.apprestaurante.databinding.ItemSalesDocumentBinding
import com.example.apprestaurante.domain.model.DocumentType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesDocumentAdapter(
    private val onClick: (SalesDocumentEntity) -> Unit
) : ListAdapter<SalesDocumentEntity, SalesDocumentAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSalesDocumentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSalesDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE"))

        fun bind(item: SalesDocumentEntity) {
            binding.tvNumber.text = item.formattedNumber
            binding.tvType.text = DocumentType.from(item.documentType).label
            binding.tvClient.text = item.clientName
            binding.tvDate.text = dateFormat.format(Date(item.issuedAt))
            binding.tvTotal.text = PriceFormatter.format(item.total)
            binding.root.setOnClickListener { onClick(item) }
            binding.btnOpen.setOnClickListener { onClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<SalesDocumentEntity>() {
        override fun areItemsTheSame(oldItem: SalesDocumentEntity, newItem: SalesDocumentEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SalesDocumentEntity, newItem: SalesDocumentEntity) =
            oldItem == newItem
    }
}
