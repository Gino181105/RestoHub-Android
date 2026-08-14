package com.example.apprestaurante.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apprestaurante.core.ImageLoader
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.data.local.entity.ProductEntity
import com.example.apprestaurante.databinding.ItemProductBinding

class ProductAdapter(
    private val onAdd: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ProductAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductEntity) {
            binding.tvName.text = item.name
            binding.tvDescription.text = item.description
            binding.tvCategory.text = item.category
            binding.tvPrice.text = PriceFormatter.format(item.price)
            binding.tvStock.text = "Stock: ${item.stock}"
            binding.btnAdd.isEnabled = item.isActive && item.stock > 0
            binding.btnAdd.text = if (item.stock > 0) "Agregar" else "Agotado"
            ImageLoader.load(binding.imgProduct, item.imageUri)
            binding.btnAdd.setOnClickListener { onAdd(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem == newItem
    }
}
