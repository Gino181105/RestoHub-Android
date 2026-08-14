package com.example.apprestaurante.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.apprestaurante.core.ImageLoader
import com.example.apprestaurante.data.remote.dto.RecipeDto
import com.example.apprestaurante.databinding.ItemRecipeBinding

class RecipeAdapter : ListAdapter<RecipeDto, RecipeAdapter.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecipeDto) {
            binding.tvName.text = item.name
            binding.tvMeta.text = "${item.cuisine} · ${item.prepTimeMinutes + item.cookTimeMinutes} min · ★ ${item.rating}"
            ImageLoader.load(binding.imgRecipe, item.image)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<RecipeDto>() {
        override fun areItemsTheSame(oldItem: RecipeDto, newItem: RecipeDto): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RecipeDto, newItem: RecipeDto): Boolean =
            oldItem == newItem
    }
}
