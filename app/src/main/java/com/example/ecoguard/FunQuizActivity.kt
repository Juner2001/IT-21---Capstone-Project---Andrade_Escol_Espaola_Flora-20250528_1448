package com.example.ecoguard

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class FunQuizActivity : AppCompatActivity() {

    data class Species(
        val title: String = "",
        val imagePath: String = ""
    )

    private lateinit var databaseRef: DatabaseReference
    private lateinit var imageView: ImageView
    private lateinit var optionButtons: List<Button>
    private lateinit var correctTitle: String
    private lateinit var scoreTextView: TextView
    private lateinit var attemptsTextView: TextView
    private lateinit var timerTextView: TextView

    private var score = 0
    private var attempts = 0
    private val maxAttempts = 10
    private val speciesList = mutableListOf<Species>()

    private var countDownTimer: CountDownTimer? = null
    private val timeLimitMillis = 20_000L
    private val countDownInterval = 1_000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun_quiz)

        imageView = findViewById(R.id.speciesImage)
        scoreTextView = findViewById(R.id.scoreTextView)
        attemptsTextView = findViewById(R.id.attemptsTextView)
        timerTextView = findViewById(R.id.timerTextView)

        optionButtons = listOf(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3),
            findViewById(R.id.option4)
        )

        databaseRef = FirebaseDatabase.getInstance().getReference("Species")

        loadSpeciesData()
    }

    private fun loadSpeciesData() {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                speciesList.clear()
                for (speciesSnapshot in snapshot.children) {
                    val title = speciesSnapshot.child("Title").getValue(String::class.java) ?: ""
                    val image = speciesSnapshot.child("ImagePath").getValue(String::class.java) ?: ""
                    if (title.isNotBlank() && image.isNotBlank()) {
                        speciesList.add(Species(title, image))
                    }
                }
                if (speciesList.size >= 4) {
                    generateQuiz()
                } else {
                    Toast.makeText(this@FunQuizActivity, "Not enough data to create a quiz.", Toast.LENGTH_LONG).show()
                    disableAllOptions()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FunQuizActivity, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun generateQuiz() {
        if (attempts >= maxAttempts) {
            cancelTimer()
            showResultDialog()
            return
        }

        cancelTimer()
        resetOptionButtons()

        val correct = speciesList.random()
        correctTitle = correct.title

        Glide.with(this)
            .load(correct.imagePath)
            .into(imageView)

        val options = speciesList
            .filter { it.title != correctTitle }
            .shuffled()
            .take(3)
            .map { it.title }
            .toMutableList()

        options.add(correctTitle)
        options.shuffle()

        for (i in optionButtons.indices) {
            val button = optionButtons[i]
            button.text = options[i]
            button.isEnabled = true
            button.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
            button.setTextColor(Color.BLACK)
            button.setOnClickListener {
                cancelTimer()
                checkAnswer(button)
            }
        }

        updateScoreAndAttemptsUI()
        startTimer()
    }

    private fun checkAnswer(selectedButton: Button) {
        disableAllOptions()
        attempts++

        if (selectedButton.text == correctTitle) {
            score++
            selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // green
            selectedButton.setTextColor(Color.WHITE)
            Toast.makeText(this, "Correct! 🎉", Toast.LENGTH_SHORT).show()
        } else {
            selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F44336")) // red
            selectedButton.setTextColor(Color.WHITE)
            Toast.makeText(this, "Wrong! The answer was $correctTitle", Toast.LENGTH_SHORT).show()
            highlightCorrectAnswer()
        }

        updateScoreAndAttemptsUI()

        selectedButton.postDelayed({
            generateQuiz()
        }, 1000)
    }

    private fun highlightCorrectAnswer() {
        for (button in optionButtons) {
            if (button.text == correctTitle) {
                button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // green
                button.setTextColor(Color.WHITE)
            }
        }
    }

    private fun disableAllOptions() {
        optionButtons.forEach { it.isEnabled = false }
    }

    private fun resetOptionButtons() {
        optionButtons.forEach {
            it.isEnabled = true
            it.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
            it.setTextColor(Color.BLACK)
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLimitMillis, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Time left: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                timerTextView.text = "Time's up!"
                disableAllOptions()
                highlightCorrectAnswer()
                attempts++
                updateScoreAndAttemptsUI()
                Toast.makeText(this@FunQuizActivity, "Time's up! The answer was $correctTitle", Toast.LENGTH_SHORT).show()

                timerTextView.postDelayed({
                    generateQuiz()
                }, 1000)
            }
        }.start()
    }


    private fun cancelTimer() {
        countDownTimer?.cancel()
        timerTextView.text = ""


    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit Quiz")
            .setMessage("Are you sure you want to exit the quiz?")
            .setPositiveButton("Yes") { _, _ ->
                cancelTimer()
                super.onBackPressed()
            }
            .setNegativeButton("No", null)
            .show()
    }


    override fun onDestroy() {
        super.onDestroy()
        cancelTimer() // Make sure to cancel the timer to avoid memory leaks or force stop issues
    }



    private fun updateScoreAndAttemptsUI() {
        scoreTextView.text = "Score: $score"
        attemptsTextView.text = "Attempts: $attempts/$maxAttempts"
    }

    private fun showResultDialog() {
        val message = "You finished the quiz!\n\nYour Score: $score / $maxAttempts\nAttempts: $attempts"

        AlertDialog.Builder(this)
            .setTitle("Quiz Complete 🎉")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Restart") { dialog, _ ->
                dialog.dismiss()
                resetQuiz()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun resetQuiz() {
        score = 0
        attempts = 0
        updateScoreAndAttemptsUI()
        generateQuiz()
    }


}
