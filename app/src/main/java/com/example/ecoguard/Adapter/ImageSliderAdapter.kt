package com.example.ecoguard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoguard.databinding.ItemImageSlideBinding

class ImageSliderAdapter(
    private val images: List<String>
) : RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(val binding: ItemImageSlideBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemImageSlideBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val imageUrl = images[position]
        Glide.with(holder.binding.root)
            .load(imageUrl)
            .into(holder.binding.slideImage)
    }

    override fun getItemCount(): Int = images.size
}
