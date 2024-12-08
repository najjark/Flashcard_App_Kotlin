package com.example.flashcardapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// Activity to handle user progress and display recent topics and how correctly each one was answered
class ProgressActivity : ComponentActivity() {

    private lateinit var progressTextView: TextView
    private lateinit var backToHomeButton: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        progressTextView = findViewById(R.id.progressTextView)

        backToHomeButton = findViewById(R.id.backToHomeButton)

        fetchUserProgress()

        backToHomeButton.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun fetchUserProgress() {
        val progressRef = db.collection("user_progress")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)

        progressRef.get()
            .addOnSuccessListener { result ->
                val topicProgress = mutableMapOf<String, IntArray>()

                for (document in result) {
                    val isCorrect = document.getBoolean("isCorrect") ?: false
                    val topic = document.getString("topic") ?: "Unknown Topic"

                    if (topicProgress.containsKey(topic)) {
                        val currentData = topicProgress[topic]!!
                        currentData[0] += 1
                        if (isCorrect) currentData[1] += 1
                    } else {
                        topicProgress[topic] = intArrayOf(1, if (isCorrect) 1 else 0)
                    }
                }
                displayProgress(topicProgress)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching progress: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayProgress(topicProgress: Map<String, IntArray>) {
        var progressText = ""
        topicProgress.entries.take(10).forEach { (topic, counts) ->
            val total = counts[0]
            val correct = counts[1]
            val ratio = if (total > 0) {
                "${(correct.toFloat() / total.toFloat() * 100).toInt()}%"
            } else {
                "No progress yet"
            }
            progressText += "$topic: Correct answers: $correct / $total (Ratio: $ratio)\n\n"
        }

        progressTextView.alpha = 0f
        progressTextView.text = progressText
        progressTextView.animate().alpha(1f).setDuration(500).start()
    }
}