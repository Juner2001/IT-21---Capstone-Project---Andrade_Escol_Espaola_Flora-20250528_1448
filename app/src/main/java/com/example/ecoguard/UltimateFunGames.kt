package com.example.ecoguard

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import android.widget.LinearLayout


class UltimateFunGames : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var btnMuteUnmute: ImageButton
    private var isMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ultimate_fun_games)

        val gifBackground: ImageView = findViewById(R.id.gifBackground)
        Glide.with(this)
            .asGif()
            .load(R.drawable.live) // make sure live.gif is in res/drawable
            .into(gifBackground)

        val btnQuizPuzzleFun: LinearLayout = findViewById(R.id.btnQuizPuzzleFun)
        val btnAnotherFunGame: LinearLayout = findViewById(R.id.btnAnotherFunGame)
        btnMuteUnmute = findViewById(R.id.btnMuteUnmute)

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music) // must be in res/raw
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        btnQuizPuzzleFun.setOnClickListener {
            val intent = Intent(this, FunQuizActivity::class.java)
            startActivity(intent)
        }

        btnAnotherFunGame.setOnClickListener {
            val intent = Intent(this, PuzzleLevelActivity::class.java)
            startActivity(intent)
        }

        btnMuteUnmute.setOnClickListener {
            if (isMuted) {
                mediaPlayer.start()
                btnMuteUnmute.setImageResource(R.drawable.ic_volume_up)
            } else {
                mediaPlayer.pause()
                btnMuteUnmute.setImageResource(R.drawable.ic_volume_off)
            }
            isMuted = !isMuted
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isMuted) {
            mediaPlayer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
