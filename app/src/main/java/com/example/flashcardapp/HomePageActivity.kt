package com.example.flashcardapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

// Activity to handle all functionality for the homepage, like navigating to the LLM generated flashcards
// or the progress tracker page as well as allowing for logging out
class HomePageActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var logoutButton: Button
    private lateinit var generateFlashcardsButton: Button
    private lateinit var topicEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var progressTrackerButton: Button
    private lateinit var randomTopicButton: Button
    private lateinit var createFlashcardButton: Button
    private lateinit var viewTopicListButton:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        generateFlashcardsButton = findViewById(R.id.generateFlashcardsButton)
        topicEditText = findViewById(R.id.topicEditText)
        numberEditText = findViewById(R.id.numberEditText)
        randomTopicButton = findViewById(R.id.randomTopicButton)
        createFlashcardButton = findViewById(R.id.createFlashcardButton)
        viewTopicListButton = findViewById(R.id.viewUserFlashcardButton)
        auth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.logoutButton)

        logoutButton.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            finish()
        }

        generateFlashcardsButton.setOnClickListener {
            val topic = topicEditText.text.toString().trim().lowercase()
            val numberOfCards = numberEditText.text.toString().toIntOrNull() ?: 5

            if (topic.isEmpty()) {
                topicEditText.error = "Please enter a topic"
                return@setOnClickListener
            }

            if (numberOfCards !in 1..10) {
                Toast.makeText(this, "Please request between 1 and 10 flashcards.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, LLMFlashcardActivity::class.java)
                intent.putExtra("TOPIC", topic)
                intent.putExtra("NUMBER_OF_CARDS", numberOfCards)
                intent.putExtra("IS_SAVED_FLASHCARDS", true)
                startActivity(intent)
            }
        }

        randomTopicButton.setOnClickListener {
            val randomTopic = RandomTopics.selectRandomTopic()
            val numberOfCards = numberEditText.text.toString().toIntOrNull() ?: 5

            Toast.makeText(
                this,
                "Generating flashcards for the topic: $randomTopic",
                Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(this, LLMFlashcardActivity::class.java)
            intent.putExtra("TOPIC", randomTopic)
            intent.putExtra("NUMBER_OF_CARDS", numberOfCards)
            startActivity(intent)
        }

        createFlashcardButton.setOnClickListener {
            val intent = Intent(this, CreateFlashcardActivity::class.java)
            startActivity(intent)
        }

        viewTopicListButton.setOnClickListener {
            val intent = Intent(this, TopicListActivity::class.java)
            startActivity(intent)
        }

        progressTrackerButton = findViewById(R.id.progressTrackerButton)

        progressTrackerButton.setOnClickListener {
            val intent = Intent(this, ProgressActivity::class.java)
            startActivity(intent)
        }
    }
}