package com.example.ecoguard

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.ecoguard.adapter.StoryContentAdapter
import java.util.*

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var tts: TextToSpeech
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: StoryContentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)

        val title = intent.getStringExtra("TITLE") ?: ""
        val author = intent.getStringExtra("AUTHOR") ?: ""
        val content = intent.getStringExtra("CONTENT") ?: ""
        val coverUrl = intent.getStringExtra("COVER_URL") ?: ""

        val pages = content.chunked(800)

        viewPager = findViewById(R.id.viewPagerContent)
        adapter = StoryContentAdapter(title, author, coverUrl, pages) { textToRead ->
            speakOut(textToRead)
        }

        viewPager.adapter = adapter

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
            }
        }

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            tts.stop()
            finish()
        }
    }

    private fun speakOut(text: String) {
        tts.stop()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}
