package com.example.apprestaurante.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apprestaurante.R
import com.example.apprestaurante.data.local.entity.UserEntity
import com.example.apprestaurante.databinding.ItemUserBinding
import com.example.apprestaurante.domain.model.UserRole

class UserAdapter(
    private val onEdit: (UserEntity) -> Unit,
    private val onToggle: (UserEntity) -> Unit
) : ListAdapter<UserEntity, UserAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserEntity) {
            val role = runCatching { UserRole.valueOf(item.role) }.getOrDefault(UserRole.CLIENT)
            binding.tvName.text = item.fullName
            binding.tvRole.text = role.label
            binding.tvContact.text = "${item.email} · ${item.phone.ifBlank { "Sin teléfono" }}"
            binding.tvDocument.text = if (item.documentNumber.isBlank()) {
                "Sin documento registrado"
            } else {
                "${item.documentType}: ${item.documentNumber}"
            }
            binding.tvStatus.text = if (item.isActive) "ACTIVO" else "INACTIVO"
            binding.tvStatus.setTextColor(
                binding.root.context.getColor(if (item.isActive) R.color.success else R.color.danger)
            )
            binding.btnToggle.text = if (item.isActive) "Desactivar" else "Activar"
            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnToggle.setOnClickListener { onToggle(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<UserEntity>() {
        override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserEntity, newItem: UserEntity) = oldItem == newItem
    }
}
