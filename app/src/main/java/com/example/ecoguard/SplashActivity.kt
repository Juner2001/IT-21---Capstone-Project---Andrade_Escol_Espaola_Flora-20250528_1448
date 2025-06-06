package com.example.ecoguard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private var progressStatus = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)

        // Simulate loading
        Thread {
            while (progressStatus < 100) {
                progressStatus += 1
                handler.post {
                    progressBar.progress = progressStatus
                    tvProgress.text = "$progressStatus%"
                }
                try {
                    Thread.sleep(30) // speed of loading
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            // When loading is done, start UltimateFunGames
            val intent = Intent(this@SplashActivity, UltimateFunGames::class.java)
            startActivity(intent)
            finish()
        }.start()
    }
}
