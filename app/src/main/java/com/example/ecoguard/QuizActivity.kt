package com.example.ecoguard

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class QuizActivity : AppCompatActivity() {

    private lateinit var quizImage: ImageView
    private lateinit var questionTitle: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var submitButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView

    private lateinit var database: DatabaseReference

    private var currentQuestion: Species? = null
    private var allTitles = mutableListOf<String>()
    private var score = 0

    private var countDownTimer: CountDownTimer? = null
    private val timeLimitMillis = 20_000L  // 20 seconds
    private val countDownInterval = 1_000L // 1 second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        quizImage = findViewById(R.id.quizImage)
        questionTitle = findViewById(R.id.questionTitle)
        optionsGroup = findViewById(R.id.optionsGroup)
        submitButton = findViewById(R.id.submitButton)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)

        database = FirebaseDatabase.getInstance().reference.child("Species")

        loadAllTitles()

        submitButton.setOnClickListener {
            val selectedOptionId = optionsGroup.checkedRadioButtonId
            if (selectedOptionId != -1) {
                countDownTimer?.cancel()
                val selectedOption = findViewById<RadioButton>(selectedOptionId)
                checkAnswer(selectedOption)
            } else {
                Toast.makeText(this, "Please select an option!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAllTitles() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    allTitles.clear()
                    for (speciesSnapshot in snapshot.children) {
                        val species = speciesSnapshot.getValue(Species::class.java)
                        species?.Title?.let {
                            allTitles.add(it)
                        }
                    }
                    loadQuestion()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@QuizActivity, "Failed to load data.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadQuestion() {
        resetOptions()

        val randomIndex = (0 until allTitles.size).random()
        val randomTitle = allTitles[randomIndex]

        database.orderByChild("Title").equalTo(randomTitle).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val species = snapshot.children.first().getValue(Species::class.java)
                        species?.let {
                            currentQuestion = it
                            Picasso.get().load(it.ImagePath).into(quizImage)
                            questionTitle.text = "Which animal is this?"
                            resizeTitle(it.Title)

                            val options = mutableListOf(it.Title)
                            while (options.size < 4) {
                                val randomOption = allTitles.random()
                                if (randomOption != it.Title && randomOption !in options) {
                                    options.add(randomOption)
                                }
                            }
                            options.shuffle()

                            val optionRadioButtons = listOf(R.id.option1, R.id.option2, R.id.option3, R.id.option4)
                            for (i in optionRadioButtons.indices) {
                                val radioButton = findViewById<RadioButton>(optionRadioButtons[i])
                                radioButton.text = options[i]
                                radioButton.isEnabled = true
                                radioButton.setTextColor(Color.BLACK)
                            }

                            scoreTextView.text = "Score: $score"
                            submitButton.isEnabled = true

                            startTimer()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@QuizActivity, "Failed to load question data.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        timerTextView.text = "Time left: 20s"
        submitButton.isEnabled = true

        countDownTimer = object : CountDownTimer(timeLimitMillis, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "Time left: ${secondsLeft}s"
            }

            override fun onFinish() {
                timerTextView.text = "Time's up!"
                submitButton.isEnabled = false

                disableOptions()
                showCorrectAnswer()

                quizImage.postDelayed({
                    loadQuestion()
                }, 2000)
            }
        }.start()
    }

    private fun checkAnswer(selectedOption: RadioButton) {
        val correctAnswer = currentQuestion?.Title ?: return

        disableOptions()
        submitButton.isEnabled = false
        countDownTimer?.cancel()

        if (selectedOption.text.toString() == correctAnswer) {
            score++
            selectedOption.setTextColor(Color.GREEN)
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
        } else {
            selectedOption.setTextColor(Color.RED)
            Toast.makeText(this, "Wrong! The correct answer is: $correctAnswer", Toast.LENGTH_SHORT).show()
            showCorrectAnswer()
        }

        scoreTextView.text = "Score: $score"

        quizImage.postDelayed({
            loadQuestion()
        }, 2000)
    }

    private fun showCorrectAnswer() {
        val correctAnswer = currentQuestion?.Title ?: return
        for (i in 0 until optionsGroup.childCount) {
            val radioButton = optionsGroup.getChildAt(i) as RadioButton
            if (radioButton.text == correctAnswer) {
                radioButton.setTextColor(Color.GREEN)
                break
            }
        }
    }

    private fun disableOptions() {
        for (i in 0 until optionsGroup.childCount) {
            val radioButton = optionsGroup.getChildAt(i) as RadioButton
            radioButton.isEnabled = false
        }
    }

    private fun resetOptions() {
        optionsGroup.clearCheck()
        for (i in 0 until optionsGroup.childCount) {
            val radioButton = optionsGroup.getChildAt(i) as RadioButton
            radioButton.setTextColor(Color.BLACK)
            radioButton.isEnabled = true
        }
    }

    private fun resizeTitle(title: String) {
        val titleLength = title.length
        when {
            titleLength <= 10 -> questionTitle.textSize = 24f
            titleLength in 11..20 -> questionTitle.textSize = 20f
            else -> questionTitle.textSize = 16f
        }
    }
}

data class Species(
    val Id: Int = 0,
    val Title: String = "",
    val Description: String = "",
    val ImagePath: String = "",
    val Price: Double = 0.0,
    val Star: Double = 0.0,
    val TimeValue: Int = 0
)
