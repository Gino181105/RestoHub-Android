package com.example.apprestaurante.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apprestaurante.R
import com.example.apprestaurante.core.ImageLoader
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.data.local.entity.ProductEntity
import com.example.apprestaurante.databinding.ItemReceptionProductBinding

class ReceptionProductAdapter(
    private val onEdit: (ProductEntity) -> Unit,
    private val onToggle: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ReceptionProductAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReceptionProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemReceptionProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductEntity) {
            binding.tvName.text = item.name
            binding.tvMeta.text = "${item.category} · ${PriceFormatter.format(item.price)}"
            binding.tvStock.text = "Stock: ${item.stock}"
            binding.tvStatus.text = if (item.isActive) "ACTIVO" else "INACTIVO"
            binding.tvStatus.setTextColor(
                binding.root.context.getColor(if (item.isActive) R.color.success else R.color.danger)
            )
            binding.tvStatus.setBackgroundResource(
                if (item.isActive) R.drawable.bg_status_active else R.drawable.bg_status_inactive
            )
            binding.btnToggle.text = if (item.isActive) "Desactivar" else "Activar"
            ImageLoader.load(binding.imgProduct, item.imageUri)
            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnToggle.setOnClickListener { onToggle(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem == newItem
    }
}
