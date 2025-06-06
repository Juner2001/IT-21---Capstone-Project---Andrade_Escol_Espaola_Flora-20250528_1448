package com.example.ecoguard

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.ecoguard.Domain.Species

class LegalDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal_detail)

        val species = intent.getSerializableExtra("object") as? Species

        val titleTxt: TextView = findViewById(R.id.titleTxt)
        val imageView: ImageView = findViewById(R.id.imageView)
        val ngoDescriptionTxt: TextView = findViewById(R.id.ngoDescriptionTxt)
        val lawDescriptionTxt: TextView = findViewById(R.id.lawDescriptionTxt)

        species?.let {
            titleTxt.text = it.title
            Glide.with(this).load(it.imagePath).into(imageView)

            // Set NGO text or default message
            ngoDescriptionTxt.text = it.ngo ?: "No NGO information available"

            // Set Law text or default message
            lawDescriptionTxt.text = it.law ?: "No Law information available"
        }
    }
}
