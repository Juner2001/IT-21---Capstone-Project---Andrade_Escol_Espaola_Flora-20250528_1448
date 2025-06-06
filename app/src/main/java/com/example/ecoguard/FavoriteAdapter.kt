package com.example.ecoguard

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoguard.Domain.Species
import com.example.ecoguard.databinding.ItemFavoriteBinding

class FavoriteAdapter(
    private val favoriteList: List<Species>,
    private val onUnfavoriteClick: (Species) -> Unit,
    private val onItemClick: (Species) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favoriteList[position])
    }

    override fun getItemCount(): Int = favoriteList.size

    inner class FavoriteViewHolder(private val binding: ItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: Species) {
            binding.favoriteTitle.text = food.title

            Glide.with(binding.favoriteImage.context)
                .load(food.imagePath)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.favoriteImage)

            binding.unfavoriteBtn.setOnClickListener {
                onUnfavoriteClick(food)
            }

            // Set an onClickListener for the whole item to open DetailActivity
            binding.root.setOnClickListener {
                onItemClick(food)
            }
        }
    }
}
