package com.example.ecoguard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Intro : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro) // Ensure an XML layout named `activity_intro.xml` exists in res/layout

        // Initialize views and set up listeners
        setupViews()
    }

    private fun setupViews() {
        // Code to initialize and interact with UI components
        // Example: findViewById<Button>(R.id.startButton).setOnClickListener { /* Navigate or perform action */ }
    }
}
