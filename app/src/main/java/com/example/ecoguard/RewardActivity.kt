package com.example.ecoguard

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ecoguard.databinding.ActivityRewardBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class RewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var rewardedAd: RewardedAd? = null
    private val TAG = "RewardActivity"
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917" // Test Ad Unit

    private var currentPoints = 0
    private var accessExpiryTime: Long = 0L
    private var lastDailyRewardTime: Long = 0L
    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Rewards"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        if (auth.currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        MobileAds.initialize(this) {}
        loadRewardedAd()
        loadUserData()

        binding.dailyRewardButton.setOnClickListener {
            claimDailyReward()
        }

        binding.watchAdButton.setOnClickListener {
            if (rewardedAd != null) {
                rewardedAd?.show(this) { _: RewardItem ->
                    updatePoints(100)
                    Toast.makeText(this, "You earned 100 points (Ad)!", Toast.LENGTH_SHORT).show()
                    loadRewardedAd()
                }
            } else {
                Toast.makeText(this, "Ad not ready, try again later.", Toast.LENGTH_SHORT).show()
                loadRewardedAd()
            }
        }

        binding.redeem100Button.setOnClickListener {
            if (currentPoints >= 100) {
                updatePoints(-100)
                extendAccessByHours(1)
                Toast.makeText(this, "Redeemed 1 hour access!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Not enough points", Toast.LENGTH_SHORT).show()
            }
        }

        binding.redeem200Button.setOnClickListener {
            if (currentPoints >= 200) {
                updatePoints(-200)
                extendAccessByHours(2)
                Toast.makeText(this, "Redeemed 2 hours access!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Not enough points", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun claimDailyReward() {
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000

        if (currentTime - lastDailyRewardTime >= oneDayMillis) {
            updatePoints(50)
            lastDailyRewardTime = currentTime
            val userId = auth.currentUser!!.uid
            database.child("users").child(userId).child("lastDailyRewardTime").setValue(lastDailyRewardTime)
            Toast.makeText(this, "You earned 50 points (Daily Reward)!", Toast.LENGTH_SHORT).show()
        } else {
            val hoursLeft = (oneDayMillis - (currentTime - lastDailyRewardTime)) / (1000 * 60 * 60)
            Toast.makeText(this, "Daily reward already claimed. Try again in $hoursLeft hours.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser!!.uid
        database.child("users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentPoints = snapshot.child("points").getValue(Int::class.java) ?: 0
                    accessExpiryTime = snapshot.child("accessExpiryTime").getValue(Long::class.java) ?: 0L
                    lastDailyRewardTime = snapshot.child("lastDailyRewardTime").getValue(Long::class.java) ?: 0L
                    updateUI()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to read user data: ${error.message}")
                }
            })
    }

    private fun updatePoints(delta: Int) {
        val userId = auth.currentUser!!.uid
        currentPoints += delta
        database.child("users").child(userId).child("points").setValue(currentPoints)
    }

    private fun extendAccessByHours(hours: Int) {
        val userId = auth.currentUser!!.uid
        val currentTime = System.currentTimeMillis()
        val newExpiry = if (accessExpiryTime > currentTime) {
            accessExpiryTime + (hours * 60 * 60 * 1000)
        } else {
            currentTime + (hours * 60 * 60 * 1000)
        }
        accessExpiryTime = newExpiry
        database.child("users").child(userId).child("accessExpiryTime").setValue(accessExpiryTime)
    }

    private fun updateUI() {
        binding.rewardPointsText.text = "Points: $currentPoints"

        val now = System.currentTimeMillis()
        if (accessExpiryTime > now) {
            val diffMillis = accessExpiryTime - now
            updateCountdownText(diffMillis)
            startCountdown(diffMillis)
        } else {
            binding.totalHoursText.text = "Access expired"
            countdownTimer?.cancel()
        }
    }

    private fun startCountdown(millisLeft: Long) {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(millisLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdownText(millisUntilFinished)
            }

            override fun onFinish() {
                binding.totalHoursText.text = "Access expired"
            }
        }.start()
    }

    private fun updateCountdownText(millisUntilFinished: Long) {
        val seconds = millisUntilFinished / 1000
        val years = seconds / (365 * 24 * 3600)
        val months = (seconds % (365 * 24 * 3600)) / (30 * 24 * 3600)
        val days = (seconds % (30 * 24 * 3600)) / (24 * 3600)
        val hours = (seconds % (24 * 3600)) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        val displayText = when {
            years > 0 -> String.format("Access: %dy %dm %dd %02dh %02dm %02ds", years, months, days, hours, minutes, secs)
            months > 0 -> String.format("Access: %dm %dd %02dh %02dm %02ds", months, days, hours, minutes, secs)
            days > 0 -> String.format("Access: %dd %02dh %02dm %02ds", days, hours, minutes, secs)
            else -> String.format("Access: %02dh %02dm %02ds", hours, minutes, secs)
        }

        binding.totalHoursText.text = displayText
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(this, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "Ad failed to load: ${adError.message}")
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad loaded.")
                rewardedAd = ad
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}
