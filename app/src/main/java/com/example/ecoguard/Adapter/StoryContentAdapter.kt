package com.example.ecoguard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoguard.R

class StoryContentAdapter(
    private val title: String,
    private val author: String,
    private val coverUrl: String,
    private val pages: List<String>,
    private val onSpeakClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_FIRST = 0
        private const val TYPE_CONTENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_FIRST else TYPE_CONTENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_FIRST) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_story_first_page, parent, false)
            FirstPageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_story_content_page, parent, false)
            ContentPageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val content = pages[position]
        if (holder is FirstPageViewHolder) {
            holder.titleText.text = title
            holder.authorText.text = "By $author"
            holder.contentText.text = content
            Glide.with(holder.itemView.context)
                .load(coverUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.coverImage)

            holder.speakButton.setOnClickListener {
                onSpeakClick(content)
            }

        } else if (holder is ContentPageViewHolder) {
            holder.contentText.text = content
            holder.speakButton.setOnClickListener {
                onSpeakClick(content)
            }
        }
    }

    override fun getItemCount(): Int = pages.size

    class FirstPageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coverImage: ImageView = view.findViewById(R.id.imageViewCover)
        val titleText: TextView = view.findViewById(R.id.textViewTitle)
        val authorText: TextView = view.findViewById(R.id.textViewAuthor)
        val contentText: TextView = view.findViewById(R.id.textViewContent)
        val speakButton: ImageButton = view.findViewById(R.id.btnSpeak)
    }

    class ContentPageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentText: TextView = view.findViewById(R.id.textViewContentOnly)
        val speakButton: ImageButton = view.findViewById(R.id.btnSpeak)
    }
}
