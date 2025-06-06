package com.example.ecoguard.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ecoguard.Model.StoryBook
import com.example.ecoguard.R
import com.example.ecoguard.StoryDetailActivity

class StoryBooksAdapter(
    private val context: Context,
    private val storyBooks: List<StoryBook>
) : RecyclerView.Adapter<StoryBooksAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewCover: ImageView = view.findViewById(R.id.imageViewCover)
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_story_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val storyBook = storyBooks[position]

        // Load image using Glide with placeholder
        Glide.with(context)
            .load(storyBook.coverUrl)
            .placeholder(R.drawable.placeholder_image)  // Placeholder image
            .error(R.drawable.placeholder_image)  // Fallback in case of error
            .into(holder.imageViewCover)

        holder.textViewTitle.text = storyBook.title

        // OnClickListener to open story details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, StoryDetailActivity::class.java).apply {
                putExtra("TITLE", storyBook.title)
                putExtra("AUTHOR", storyBook.author)
                putExtra("CONTENT", storyBook.content)
                putExtra("COVER_URL", storyBook.coverUrl)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = storyBooks.size
}
