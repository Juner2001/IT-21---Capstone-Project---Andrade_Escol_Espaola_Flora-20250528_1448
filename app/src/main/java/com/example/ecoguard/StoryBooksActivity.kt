package com.example.ecoguard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ecoguard.Model.StoryBook
import com.example.ecoguard.adapter.StoryBooksAdapter
import com.google.firebase.database.*

class StoryBooksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StoryBooksAdapter
    private lateinit var database: DatabaseReference
    private val storyBooksList = mutableListOf<StoryBook>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_books)

        recyclerView = findViewById(R.id.recyclerViewStoryBooks)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // Adjust column count if needed

        adapter = StoryBooksAdapter(this, storyBooksList)
        recyclerView.adapter = adapter

        // Initialize Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance().getReference("stories")

        // Fetch stories from Firebase
        fetchStories()
    }

    private fun fetchStories() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storyBooksList.clear()
                for (storySnapshot in snapshot.children) {
                    val story = storySnapshot.getValue(StoryBook::class.java)
                    if (story != null) {
                        storyBooksList.add(story)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load stories: ${error.message}")
            }
        })
    }
}
