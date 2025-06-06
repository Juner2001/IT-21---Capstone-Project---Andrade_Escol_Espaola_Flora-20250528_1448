package com.example.ecoguard


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ecoguard.LoginActivity
import com.example.ecoguard.MainActivity
import com.google.firebase.auth.FirebaseAuth

class BrandActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Removed setContentView(R.layout.activity_brand)

        splashScreen.setKeepOnScreenCondition { true }

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            val nextActivity = if (currentUser == null) {
                LoginActivity::class.java
            } else {
                MainActivity::class.java
            }
            startActivity(Intent(this, nextActivity))
            finish()
        }, 2000)
    }
}
