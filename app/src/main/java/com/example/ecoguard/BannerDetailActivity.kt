package com.example.ecoguard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.ecoguard.databinding.ActivityBannerDetailBinding

class BannerDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBannerDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBannerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val imageUrl = intent.getStringExtra("imageUrl")
        val title = intent.getStringExtra("title")
        val newsMeta = intent.getStringExtra("newsMeta")
        val description = intent.getStringExtra("description")


        // Set title and description
        binding.bannerTitleTextView.text = title ?: "No Title"
        binding.bannerExtraTextView.text = newsMeta ?: "N/A"
        binding.bannerDescriptionTextView.text = description ?: "No Description"



        // Load image with rounded corners using Glide
        if (!imageUrl.isNullOrEmpty()) {
            val radiusInPx = resources.getDimensionPixelSize(R.dimen.image_corner_radius)
            Glide.with(this)
                .load(imageUrl)
                .transform(RoundedCorners(radiusInPx))
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_quiz_puzzle)
                .into(binding.bannerImageView)
        } else {
            binding.bannerImageView.setImageResource(R.drawable.ic_placeholder)
        }

        // Handle back button
        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
