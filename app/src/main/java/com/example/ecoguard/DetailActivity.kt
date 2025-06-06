package com.example.ecoguard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.ecoguard.Domain.Species
import com.example.ecoguard.databinding.ActivityDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var species: Species
    private var isFavorite = false

    // Firebase
    private var database: DatabaseReference? = null
    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser }

    // Auto-slide variables
    private val sliderHandler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private lateinit var sliderRunnable: Runnable
    private lateinit var sliderAdapter: ImageSliderAdapter

    // Text-to-Speech
    private lateinit var textToSpeech: TextToSpeech

    // User points and access expiry
    private var userPoints: Int = 0
    private var accessExpiryTime: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = resources.getColor(R.color.black, null)

        // Immediately lock details and disable buttons (no flicker)
        clearDetails()
        disableButtons()

        // Init TTS
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "TTS language not supported", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Firebase reference for favorites
        currentUser?.let { user ->
            database = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.uid)
                .child("favorites")
        }

        species = intent.getSerializableExtra("object") as? Species
            ?: throw IllegalArgumentException("Species object missing in Intent")

        val images = species.imagePaths?.takeIf { it.isNotEmpty() }
            ?: listOfNotNull(species.imagePath)

        setupImageSlider(images)

        // Check points and access expiry and unlock details if valid
        checkPointsAndUnlock()

        // Back button
        binding.backBtn.setOnClickListener { finish() }
    }

    private fun setupImageSlider(images: List<String>) {
        sliderAdapter = ImageSliderAdapter(images)
        binding.imageSlider.apply {
            adapter = sliderAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        sliderRunnable = object : Runnable {
            override fun run() {
                if (sliderAdapter.itemCount == 0) return
                currentPage = (currentPage + 1) % sliderAdapter.itemCount
                binding.imageSlider.setCurrentItem(currentPage, true)
                sliderHandler.postDelayed(this, 2000)
            }
        }

        sliderHandler.postDelayed(sliderRunnable, 2000)

        binding.imageSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 2000)
            }
        })
    }

    private fun checkPointsAndUnlock() {
        loadAccessExpiryTime {
            loadUserPoints {
                val now = System.currentTimeMillis()
                val hasValidAccess = accessExpiryTime?.let { expiry -> expiry > now } ?: false

                if (userPoints >= 10 || hasValidAccess) {
                    if (!hasValidAccess) {
                        // Deduct points only if no valid access expiry
                        deductPoints(10) {
                            runOnUiThread {
                                bindData()
                                checkIfFavorite()
                                enableButtons()
                            }
                        }
                    } else {
                        runOnUiThread {
                            bindData()
                            checkIfFavorite()
                            enableButtons()
                        }
                    }
                } else {
                    runOnUiThread {
                        showInsufficientPointsDialog()
                        clearDetails()
                        disableButtons()
                    }
                }
            }
        }
    }

    private fun loadAccessExpiryTime(onLoaded: () -> Unit) {
        val userId = currentUser?.uid ?: return onLoaded()
        val accessRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("accessExpiryTime")

        accessRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                accessExpiryTime = snapshot.getValue(Long::class.java)
                onLoaded()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailActivity, "Failed to load access expiry", Toast.LENGTH_SHORT).show()
                onLoaded()
            }
        })
    }

    private fun loadUserPoints(onLoaded: () -> Unit) {
        val userId = currentUser?.uid ?: return onLoaded()
        val pointsRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("points")

        pointsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userPoints = snapshot.getValue(Int::class.java) ?: 0
                onLoaded()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailActivity, "Failed to load points", Toast.LENGTH_SHORT).show()
                onLoaded()
            }
        })
    }

    private fun deductPoints(pointsToDeduct: Int, onComplete: () -> Unit) {
        val userId = currentUser?.uid ?: return
        val pointsRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("points")

        val newPoints = userPoints - pointsToDeduct
        pointsRef.setValue(newPoints).addOnSuccessListener {
            userPoints = newPoints
            Toast.makeText(this, "$pointsToDeduct points deducted. You have $userPoints points left.", Toast.LENGTH_SHORT).show()
            onComplete()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update points.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showInsufficientPointsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Insufficient Points")
            .setMessage("Walang sapat na points para ma-unlock ang species details. Mag-ipon muna ng points.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun bindData() {
        binding.titleTxt.text = species.title
        binding.descriptionTxt.text = species.description
        binding.fnTxt.text = species.fn
        binding.snTxt.text = species.sn
        binding.enTxt.text = species.en
        binding.tgTxt.text = species.tg
        binding.cyTxt.text = species.cn
        binding.vsTxt.text = species.vn
        binding.sizeTxt.text = species.size
        binding.statusTxt.text = species.status
        binding.lifespanTxt.text = species.lifespan

        binding.favBtn.isEnabled = true
        binding.speakBtn.isEnabled = true

        binding.favBtn.setOnClickListener { toggleFavorite() }

        binding.speakBtn.setOnClickListener {
            speakDescription()
        }
    }

    private fun clearDetails() {
        // Clear or mask details when no access
        binding.titleTxt.text = "Locked"
        binding.descriptionTxt.text = "Unlock to see description"
        binding.fnTxt.text = "-"
        binding.snTxt.text = "-"
        binding.enTxt.text = "-"
        binding.tgTxt.text = "-"
        binding.cyTxt.text = "-"
        binding.vsTxt.text = "-"
        binding.sizeTxt.text = "-"
        binding.statusTxt.text = "-"
        binding.lifespanTxt.text = "-"
    }

    private fun enableButtons() {
        binding.favBtn.isEnabled = true
        binding.speakBtn.isEnabled = true
    }

    private fun disableButtons() {
        binding.favBtn.isEnabled = false
        binding.speakBtn.isEnabled = false
    }

    private fun speakDescription() {
        val text = species.description ?: "No description available"
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun checkIfFavorite() {
        val key = species.title ?: return
        database?.child(key)
            ?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFavorite = snapshot.exists()
                    updateFavoriteIcon()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DetailActivity,
                        "Failed to load favorite status",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun toggleFavorite() {
        val db = database
        if (db == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val key = species.title ?: return
        isFavorite = !isFavorite

        if (isFavorite) {
            db.child(key).setValue(species)
                .addOnSuccessListener {
                    Toast.makeText(this, "Bookmark added!", Toast.LENGTH_SHORT).show()
                    updateFavoriteIcon()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add bookmark", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.child(key).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Bookmark removed!", Toast.LENGTH_SHORT).show()
                    updateFavoriteIcon()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to remove bookmark", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            binding.favBtn.setImageResource(R.drawable.ic_bookmark_added)
        } else {
            binding.favBtn.setImageResource(R.drawable.ic_bookmark_unadded)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sliderHandler.removeCallbacks(sliderRunnable)
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}
