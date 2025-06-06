package com.example.ecoguard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecoguard.adapter.SuggestionsAdapter
import com.example.ecoguard.databinding.ActivitySearchBinding
import com.example.ecoguard.Domain.Species
import com.google.firebase.database.*

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var database: DatabaseReference
    private val suggestions = mutableListOf<Species>()
    private lateinit var suggestionsAdapter: SuggestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("Species")

        // Initialize the adapter with click listener for redirection
        suggestionsAdapter = SuggestionsAdapter(suggestions) { selectedFood ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("object", selectedFood) // Pass the Foods object to DetailActivity
            startActivity(intent)
        }
        binding.suggestionsList.layoutManager = LinearLayoutManager(this)
        binding.suggestionsList.adapter = suggestionsAdapter

        // Focus on search bar and show keyboard
        binding.searchBar.requestFocus()
        showKeyboard()

        // Listen for text changes in the search bar
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                if (query.isNotEmpty()) {
                    searchDatabase(query)
                } else {
                    clearSuggestions()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchBar, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
    }

    private fun clearSuggestions() {
        suggestions.clear()
        suggestionsAdapter.notifyDataSetChanged()
        binding.emptyText.visibility = View.VISIBLE
        binding.loadingIndicator.visibility = View.GONE
    }

    private fun searchDatabase(query: String) {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                suggestions.clear()
                for (snapshot in dataSnapshot.children) {
                    val food = snapshot.getValue(Species::class.java)
                    food?.let {
                        if (it.getTitle().lowercase().contains(query)) {
                            suggestions.add(it)
                        }
                    }
                }
                binding.loadingIndicator.visibility = View.GONE
                suggestionsAdapter.notifyDataSetChanged()
                binding.emptyText.visibility = if (suggestions.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                binding.loadingIndicator.visibility = View.GONE
                clearSuggestions()
            }
        })
    }
}
