package com.example.flashcardapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

// Activity that allows the user to create their own flashcards by entering the question and answer,
// As well as the topic related to the flashcard
class CreateFlashcardActivity : BaseActivity() {

    private lateinit var topicInput: EditText
    private lateinit var questionInput: EditText
    private lateinit var answerInput: EditText
    private lateinit var saveButton: Button
    private lateinit var backToHomeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createflashcard)

        topicInput = findViewById(R.id.topicInput)
        questionInput = findViewById(R.id.questionInput)
        answerInput = findViewById(R.id.answerInput)
        saveButton = findViewById(R.id.saveButton)
        backToHomeButton = findViewById(R.id.backToHomeButton)

        saveButton.setOnClickListener {
            val question = questionInput.text.toString().trim()
            val answer = answerInput.text.toString().trim()
            var topic = topicInput.text.toString().trim()

            topic = topic.lowercase()

            if (question.isNotEmpty() && answer.isNotEmpty()) {
                val flashcard = Flashcard(question, answer, isAnswered = false, isCorrect = null, isSaved = true, topic)
                saveFlashcardToFirestore(flashcard)
                clearFields()
                Toast.makeText(this, "Flashcard saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        backToHomeButton.setOnClickListener {
            finish()
        }
    }

    // Saving flashcards to the database
    private fun saveFlashcardToFirestore(flashcard: Flashcard) {
        val db = FirebaseFirestore.getInstance()
        db.collection("user_flashcards")
            .add(flashcard)
            .addOnSuccessListener {
                Log.d("CreateFlashcardActivity", "Flashcard saved successfully!")
            }
            .addOnFailureListener { e ->
                Log.w("CreateFlashcardActivity", "Error saving flashcard", e)
            }
    }

    // Clearing text fields so the user can enter content for the next flashcard
    private fun clearFields() {
        questionInput.setText("")
        answerInput.setText("")
        topicInput.setText("")
    }
}