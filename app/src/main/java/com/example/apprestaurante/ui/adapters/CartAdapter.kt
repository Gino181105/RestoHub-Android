package com.example.apprestaurante.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apprestaurante.core.ImageLoader
import com.example.apprestaurante.core.PriceFormatter
import com.example.apprestaurante.data.local.model.CartProductItem
import com.example.apprestaurante.databinding.ItemCartBinding

class CartAdapter(
    private val onIncrease: (CartProductItem) -> Unit,
    private val onDecrease: (CartProductItem) -> Unit,
    private val onRemove: (CartProductItem) -> Unit
) : ListAdapter<CartProductItem, CartAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartProductItem) {
            binding.tvName.text = item.name
            binding.tvUnitPrice.text = "Unidad: ${PriceFormatter.format(item.price)}"
            binding.tvSubtotal.text = PriceFormatter.format(item.subtotal)
            binding.tvStock.text = "Disponible: ${item.stock}"
            binding.tvQuantity.text = item.quantity.toString()
            binding.btnPlus.isEnabled = item.quantity < item.stock
            ImageLoader.load(binding.imgProduct, item.imageUri)
            binding.btnPlus.setOnClickListener { onIncrease(item) }
            binding.btnMinus.setOnClickListener {
                if (item.quantity <= 1) onRemove(item) else onDecrease(item)
            }
            binding.btnRemove.setOnClickListener { onRemove(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CartProductItem>() {
        override fun areItemsTheSame(oldItem: CartProductItem, newItem: CartProductItem): Boolean =
            oldItem.cartId == newItem.cartId
        override fun areContentsTheSame(oldItem: CartProductItem, newItem: CartProductItem): Boolean =
            oldItem == newItem
    }
}
