package com.example.ecoguard

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ViewReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_report)

        val tvSubject = findViewById<TextView>(R.id.tvSubject)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val llImages = findViewById<LinearLayout>(R.id.llImages)

        val subject = intent.getStringExtra("subject")
        val message = intent.getStringExtra("message")
        val imageUrls = intent.getStringArrayListExtra("imageUrls")

        tvSubject.text = subject
        tvMessage.text = message

        imageUrls?.forEach { url ->
            val imageView = ImageView(this)
            imageView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
            ).apply {
                setMargins(0, 16, 0, 16)
            }
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(this).load(url).into(imageView)
            llImages.addView(imageView)
        }
    }
}
