package com.example.ecoguard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.FirebaseDatabase

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingViewPager: ViewPager2
    private lateinit var buttonNext: Button
    private lateinit var buttonGetStarted: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        onboardingViewPager = findViewById(R.id.viewPagerOnboarding)
        buttonNext = findViewById(R.id.buttonNext)
        buttonGetStarted = findViewById(R.id.buttonGetStarted)

        val onboardingItems = listOf(
            OnboardingItem(R.drawable.search, "Search Function", "Use the search bar to look up marine life using their English names"),
            OnboardingItem(R.drawable.news, "News and Updates", "Stay updated with the latest marine conservation efforts and community events"),
            OnboardingItem(R.drawable.popular, "Popular Marine Life", "Features the most visited and favorited marine species."),
            OnboardingItem(R.drawable.reward, "Marine Life Access via Rewards", "To view detailed information (photos, descriptions) about a marine life entry, users must earn and use rewards."),
            OnboardingItem(R.drawable.scanning, "Scanning Feature", "Use this tool to scan QR codes or identification tags related to marine life or learning resources."),
            OnboardingItem(R.drawable.categories, "Categories of Marine Life", "Browse marine life by categories such as coral species, fish species, turtles, and more."),
            OnboardingItem(R.drawable.bookmark1, "Bookmark", "Your personalized collection of favorite or frequently visited marine life entries."),
            OnboardingItem(R.drawable.education1, "Education", "Make learning fun and interactive through various educational tools."),
            OnboardingItem(R.drawable.legalprotection, "Legal Protection", "Learn about the legal frameworks and NGOs protecting our marine ecosystem."),
            OnboardingItem(R.drawable.complaint, "Complaint", "Use this feature to report any illegal activities affecting marine life or marine environments in your area.")
        )

        onboardingViewPager.adapter = OnboardingAdapter(onboardingItems)

        buttonNext.setOnClickListener {
            val nextItem = onboardingViewPager.currentItem + 1
            if (nextItem < onboardingItems.size) {
                onboardingViewPager.currentItem = nextItem
            }
        }

        buttonGetStarted.setOnClickListener {
            val userId = intent.getStringExtra("USER_ID")
            if (userId != null) {
                FirebaseDatabase.getInstance().getReference("users/$userId/tutorialShown").setValue(true)
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        onboardingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == onboardingItems.size - 1) {
                    buttonNext.visibility = View.GONE
                    buttonGetStarted.visibility = View.VISIBLE
                } else {
                    buttonNext.visibility = View.VISIBLE
                    buttonGetStarted.visibility = View.GONE
                }
            }
        })
    }
}
