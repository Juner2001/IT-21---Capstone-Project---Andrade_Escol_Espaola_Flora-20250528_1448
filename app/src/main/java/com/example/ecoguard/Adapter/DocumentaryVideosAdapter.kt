package com.example.ecoguard.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoguard.Model.DocumentaryVideo
import com.example.ecoguard.R

class DocumentaryVideosAdapter(
    private val context: Context,
    private val videoList: List<DocumentaryVideo>,
    private val onItemClick: (DocumentaryVideo) -> Unit
) : RecyclerView.Adapter<DocumentaryVideosAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_documentary_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        holder.bind(video)
        holder.itemView.setOnClickListener { onItemClick(video) }
    }

    override fun getItemCount(): Int = videoList.size

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bannerImageView: ImageView = itemView.findViewById(R.id.imageViewBanner)
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDescription)

        fun bind(video: DocumentaryVideo) {
            titleTextView.text = video.title
            descriptionTextView.text = video.description

            // Kinuha ang video ID mula sa YouTube URL
            val videoId = getYoutubeVideoId(video.youtubeUrl)
            if (videoId != null) {
                // I-generate ang thumbnail URL batay sa YouTube video ID
                val thumbnailUrl = "https://img.youtube.com/vi/$videoId/0.jpg"
                Glide.with(itemView)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.ic_placeholder) // Palitan ayon sa iyong placeholder image
                    .into(bannerImageView)
            } else {
                // Kung hindi ma-extract ang video ID, itago ang ImageView o mag-set ng default image
                bannerImageView.visibility = View.GONE
            }
        }

        private fun getYoutubeVideoId(url: String): String? {
            return if (url.contains("v=")) {
                val parts = url.split("v=")
                val idPart = parts[1]
                val ampersandIndex = idPart.indexOf('&')
                if (ampersandIndex != -1) {
                    idPart.substring(0, ampersandIndex)
                } else {
                    idPart
                }
            } else if (url.contains("youtu.be/")) {
                url.substringAfter("youtu.be/")
            } else {
                null
            }
        }
    }
}
