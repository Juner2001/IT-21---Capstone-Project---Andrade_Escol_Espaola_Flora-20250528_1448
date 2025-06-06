package com.example.ecoguard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoguard.databinding.ItemSuggestionBinding
import com.example.ecoguard.Domain.Species

// Adapter for displaying suggestions in a RecyclerView
class SuggestionsAdapter(
    private val suggestions: List<Species>, // List of suggestions (Species)
    private val clickListener: (Species) -> Unit // Lambda function to handle item clicks
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    // Called to create a new ViewHolder, inflating the layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SuggestionViewHolder(binding) // Return a new ViewHolder with the inflated layout
    }

    // Bind the data (Species) to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(suggestions[position], clickListener) // Bind data and click listener
    }

    // Return the total count of suggestions
    override fun getItemCount() = suggestions.size

    // ViewHolder class for binding individual items
    class SuggestionViewHolder(private val binding: ItemSuggestionBinding) : RecyclerView.ViewHolder(binding.root) {

        // Bind the species data to the views and set the click listener
        fun bind(speciesItem: Species, clickListener: (Species) -> Unit) {
            binding.suggestionText.text = speciesItem.getTitle() // Set the title text

            // Set the click listener to call the provided function when the item is clicked
            binding.root.setOnClickListener { clickListener(speciesItem) }
        }
    }
}
