package com.example.apprestaurante.core

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.apprestaurante.R

object ImageLoader {
    fun load(view: ImageView, reference: String?) {
        Glide.with(view)
            .load(reference?.takeIf { it.isNotBlank() })
            .placeholder(R.drawable.bg_image_placeholder)
            .error(R.drawable.bg_image_placeholder)
            .centerCrop()
            .into(view)
    }
}
