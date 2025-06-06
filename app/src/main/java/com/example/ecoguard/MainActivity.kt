package com.example.ecoguard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private var currentSelectedItemId: Int = -1
    private var lastClickTime: Long = 0  // debounce

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || !user.isEmailVerified) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(loginIntent)
            finish()
            return
        }

        // SQLite onboarding check
        val dbHelper = AppDatabaseHelper(this)
        if (!dbHelper.isOnboardingShown()) {
            dbHelper.setOnboardingShown()
            val intent = Intent(this@MainActivity, OnboardingActivity::class.java)
            intent.putExtra("USER_ID", user.uid)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottomNavView)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val currentTime = System.currentTimeMillis()
            if (item.itemId == currentSelectedItemId && currentTime - lastClickTime < 500) {
                return@setOnNavigationItemSelectedListener false
            }
            lastClickTime = currentTime
            currentSelectedItemId = item.itemId
            onNavigationItemSelected(item)
        }

        bottomNavigationView.selectedItemId = R.id.navHome
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        val selectedFragment: Fragment = when (item.itemId) {
            R.id.navHome -> HomeFragment()
            R.id.navContact -> ReportFragment()
            R.id.navLegal -> LegalProtectionFragment()
            R.id.navFavorite -> FavoriteFragment()
            R.id.navEdu -> EduFragment()
            else -> HomeFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, selectedFragment)
            .commit()

        return true
    }
}
