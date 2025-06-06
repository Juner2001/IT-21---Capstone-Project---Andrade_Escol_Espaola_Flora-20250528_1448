package com.example.ecoguard

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecoguard.Adapter.DocumentaryVideosAdapter
import com.example.ecoguard.Model.DocumentaryVideo
import com.example.ecoguard.databinding.ActivityDocumentaryVideosBinding
import com.google.firebase.database.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class DocumentaryVideosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocumentaryVideosBinding
    private lateinit var adapter: DocumentaryVideosAdapter
    private val videoList = mutableListOf<DocumentaryVideo>()
    private var currentYouTubePlayer: YouTubePlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentaryVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycle.addObserver(binding.youtubePlayerView) // Important!

        binding.recyclerViewDocumentaries.layoutManager = LinearLayoutManager(this)
        adapter = DocumentaryVideosAdapter(this, videoList) { video ->
            playYouTubeInActivity(video.youtubeUrl)
        }
        binding.recyclerViewDocumentaries.adapter = adapter

        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                currentYouTubePlayer = youTubePlayer
            }
        })

        fetchVideosFromFirebase()
    }

    private fun fetchVideosFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("documentary_videos")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                videoList.clear()
                for (videoSnapshot in snapshot.children) {
                    val video = videoSnapshot.getValue(DocumentaryVideo::class.java)
                    if (video != null) {
                        videoList.add(video)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ", error.toException())
            }
        })
    }

    private fun playYouTubeInActivity(url: String) {
        val videoId = extractYoutubeVideoId(url)
        if (videoId != null && currentYouTubePlayer != null) {
            binding.youtubePlayerView.visibility = View.VISIBLE
            currentYouTubePlayer?.loadVideo(videoId, 0f)
        } else {
            Log.e("YouTube", "Invalid video ID or player not ready")
        }
    }

    private fun extractYoutubeVideoId(url: String): String? {
        val uri = Uri.parse(url)
        return when {
            uri.host?.contains("youtu.be") == true -> uri.lastPathSegment
            uri.host?.contains("youtube.com") == true -> uri.getQueryParameter("v")
            else -> null
        }
    }
}
