package com.example.flashcardapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

// Activity that handles user-created flashcards as well as their functionality like
// marking as correct or incorrect and navigation between flashcards
class UserFlashcardsActivity : AppCompatActivity() {

    private lateinit var flashcardText: TextView
    private lateinit var showAnswerButton: Button
    private lateinit var correctButton: Button
    private lateinit var incorrectButton: Button
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var backToFlashcardList: Button
    private lateinit var loadingIndicator: ProgressBar
    private var flashcardsList = mutableListOf<Flashcard>()
    private var currentIndex = 0
    private var isAnswerVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_flashcard)

        val topic = intent.getStringExtra("TOPIC")?.lowercase() ?: "Unknown Topic"

        flashcardText = findViewById(R.id.flashcardText)
        showAnswerButton = findViewById(R.id.showAnswerButton)
        correctButton = findViewById(R.id.correctButton)
        incorrectButton = findViewById(R.id.incorrectButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        backToFlashcardList = findViewById(R.id.backToFlashcardList)

        backToFlashcardList.setOnClickListener {
            startActivity(Intent(this, TopicListActivity::class.java))
        }

        showAnswerButton.setOnClickListener { toggleAnswerVisibility() }
        correctButton.setOnClickListener { markFlashcardCorrect(topic) }
        incorrectButton.setOnClickListener { markFlashcardIncorrect(topic) }
        previousButton.setOnClickListener { showPreviousFlashcard() }
        nextButton.setOnClickListener { showNextFlashcard() }

        val question = intent.getStringExtra("QUESTION")
        loadUserFlashcards(question)
    }

    // Function to load user flashcards and filter by topic
    @SuppressLint("SetTextI18n")
    private fun loadUserFlashcards(question: String?) {
        val db = FirebaseFirestore.getInstance()
        val topic = intent.getStringExtra("TOPIC")
        loadingIndicator.visibility = ProgressBar.VISIBLE

        db.collection("user_flashcards")
            .whereEqualTo("topic", topic)
            .get()
            .addOnSuccessListener { documents ->
                flashcardsList.clear()
                for (document in documents) {
                    val flashcard = document.toObject(Flashcard::class.java)
                    flashcardsList.add(flashcard)
                }
                loadingIndicator.visibility = View.GONE
                if (flashcardsList.isNotEmpty()) {
                    val index = flashcardsList.indexOfFirst { it.question == question }
                    currentIndex = if (index != -1) index else 0
                    displayFlashcard(currentIndex)
                    updateNavigationButtons()
                } else {
                    flashcardText.text = "No flashcards available for this topic."
                }
            }
            .addOnFailureListener { e ->
                loadingIndicator.visibility = View.GONE
                Log.w("UserFlashcardsActivity", "Error fetching flashcards", e)
                flashcardText.text = "Failed to load flashcards."
            }
    }

    // Function to display each flashcard's content, as well as fade in and out animations
    @SuppressLint("SetTextI18n")
    private fun displayFlashcard(index: Int) {
        val flashcard = flashcardsList[index]

        flashcardText.animate()
            .alpha(0f)
            .setDuration(400)
            .withEndAction {
                val newText = flashcard.question
                flashcardText.text = newText

                flashcardText.alpha = 0f

                flashcardText.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start()
            }
            .start()

        isAnswerVisible = false
        showAnswerButton.text = "Show Answer"

        updateNavigationButtons()
        updateAnswerButtonState(flashcard)
    }

    // Function to handle whether to display question or answer
    private fun toggleAnswerVisibility() {
        val flashcard = flashcardsList[currentIndex]
        val newText = if (isAnswerVisible) flashcard.question else flashcard.answer

        flashcardText.animate()
            .alpha(0f)
            .setDuration(400)
            .withEndAction {
                flashcardText.text = newText

                flashcardText.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start()
            }
            .start()

        isAnswerVisible = !isAnswerVisible
        showAnswerButton.text = if (isAnswerVisible) "Show Question" else "Show Answer"
    }

    // Function to handle navigation to the next flashcard
    private fun showNextFlashcard() {
        currentIndex = (currentIndex + 1) % flashcardsList.size
        displayFlashcard(currentIndex)
        updateNavigationButtons()
    }

    // Function to handle navigation to the previous flashcard
    private fun showPreviousFlashcard() {
        currentIndex = if (currentIndex - 1 < 0) flashcardsList.size - 1 else currentIndex - 1
        displayFlashcard(currentIndex)
        updateNavigationButtons()
    }

    // Function to handle logic for correct flashcard marking
    private fun markFlashcardCorrect(topic: String) {
        val flashcard = flashcardsList[currentIndex]
        flashcard.isAnswered = true
        flashcard.isCorrect = true
        updateAnswerButtonState(flashcard)
        saveFlashcardProgress(flashcard, true, topic)
        Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
    }

    // Function to handle logic incorrect flashcard marking
    private fun markFlashcardIncorrect(topic: String) {
        val flashcard = flashcardsList[currentIndex]
        flashcard.isAnswered = true
        flashcard.isCorrect = false
        updateAnswerButtonState(flashcard)
        saveFlashcardProgress(flashcard, false, topic)
        Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show()
    }

    // Function to handle saving flashcard progress so it can be viewed in the progress page
    private fun saveFlashcardProgress(flashcard: Flashcard, isCorrect: Boolean, topic: String) {
        val db = FirebaseFirestore.getInstance()
        val progressData = hashMapOf(
            "question" to flashcard.question,
            "isCorrect" to isCorrect,
            "topic" to topic.lowercase(),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("user_progress").add(progressData)
            .addOnSuccessListener {
                Log.d("UserFlashcardsActivity", "Progress saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("UserFlashcardsActivity", "Failed to save progress", e)
            }
    }

    // Function to handle navigation buttons animations as well as their state
    private fun updateNavigationButtons() {
        previousButton.isEnabled = currentIndex > 0
        nextButton.isEnabled = currentIndex < flashcardsList.size - 1

        previousButton.alpha = if (previousButton.isEnabled) 1.0f else 0.5f
        nextButton.alpha = if (nextButton.isEnabled) 1.0f else 0.5f

        previousButton.animate().alpha(if (previousButton.isEnabled) 1.0f else 0.5f).setDuration(400).start()
        nextButton.animate().alpha(if (nextButton.isEnabled) 1.0f else 0.5f).setDuration(400).start()
    }

    // Function to handle marking button animations as well as their state
    private fun updateAnswerButtonState(flashcard: Flashcard) {
        val enabled = !flashcard.isAnswered
        correctButton.isEnabled = enabled
        incorrectButton.isEnabled = enabled

        correctButton.animate().alpha(if (enabled) 1.0f else 0.5f).setDuration(400).start()
        incorrectButton.animate().alpha(if (enabled) 1.0f else 0.5f).setDuration(400).start()
    }
}