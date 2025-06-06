package com.example.ecoguard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PuzzleLevelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle_level)

        val btnEasy: Button = findViewById(R.id.btnEasy)
        val btnHard: Button = findViewById(R.id.btnHard)

        btnEasy.setOnClickListener {
            val intent = Intent(this, EasyPuzzleActivity::class.java)
            startActivity(intent)
        }

        btnHard.setOnClickListener {
            val intent = Intent(this, PuzzleActivity::class.java)
            startActivity(intent)
        }
    }
}
