package com.example.apprestaurante.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apprestaurante.core.ImageLoader
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.data.local.entity.OrderItemEntity
import com.example.apprestaurante.databinding.ItemOrderDetailBinding

class OrderItemAdapter :
    ListAdapter<OrderItemEntity, OrderItemAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemOrderDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderItemEntity) {
            binding.tvName.text = item.productName
            binding.tvQuantity.text = "${item.quantity} × ${PriceFormatter.format(item.unitPrice)}"
            binding.tvSubtotal.text = PriceFormatter.format(item.unitPrice * item.quantity)
            ImageLoader.load(binding.imgProduct, item.imageUri)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<OrderItemEntity>() {
        override fun areItemsTheSame(oldItem: OrderItemEntity, newItem: OrderItemEntity): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: OrderItemEntity, newItem: OrderItemEntity): Boolean =
            oldItem == newItem
    }
}
