package com.example.ecoguard.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoguard.Domain.BannerItem
import com.example.ecoguard.databinding.ItemSlidingBinding

class SlidingAdapter(
    private val items: List<BannerItem>,
    private val onItemClick: (BannerItem) -> Unit
) : RecyclerView.Adapter<SlidingAdapter.SlidingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlidingViewHolder {
        val binding = ItemSlidingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlidingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SlidingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class SlidingViewHolder(private val binding: ItemSlidingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BannerItem) {
            Glide.with(binding.root)
                .load(item.imageUrl)
                .into(binding.image)

            // Set the click listener
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
