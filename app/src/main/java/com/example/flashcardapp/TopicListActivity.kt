
package com.example.flashcardapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

// Activity to handle showing the list of flashcard topics using a RecyclerView
// Also includes logic to search and delete topics
class TopicListActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var topicAdapter: TopicAdapter
    private var topics: List<String> = emptyList()
    private lateinit var searchBar: EditText
    private lateinit var backToHomeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topics_list)
        backToHomeButton = findViewById(R.id.backToHomeButton)
        searchBar = findViewById(R.id.searchBar)

        recyclerView = findViewById(R.id.topicsRecyclerView)
        fetchTopics()

        backToHomeButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTopics(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        enableSwipeToDelete()
    }

    private fun filterTopics(query: String) {
        val matchedTopics = topics.filter { it.contains(query, ignoreCase = true) }
        topicAdapter.updateData(matchedTopics)
    }

    private fun fetchTopics() {
        val db = FirebaseFirestore.getInstance()
        db.collection("user_flashcards")
            .get()
            .addOnSuccessListener { documents ->
                topics = documents.mapNotNull { it.getString("topic") }.distinct()
                if (topics.isEmpty()) {
                    Toast.makeText(this, "No topics found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                setupRecyclerView(topics)
            }
            .addOnFailureListener { e ->
                Log.w("TopicsActivity", "Error fetching topics", e)
                Toast.makeText(this, "Failed to load topics", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView(topics: List<String>) {
        topicAdapter = TopicAdapter(topics) { topic ->
            val intent = Intent(this, FlashcardListActivity::class.java).apply {
                putExtra("SELECTED_TOPIC", topic)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = topicAdapter
    }

    private fun enableSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val removedTopic = topicAdapter.removeItem(position)
                    deleteTopicFromDatabase(removedTopic)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun deleteTopicFromDatabase(topic: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("user_flashcards")
            .whereEqualTo("topic", topic)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("user_flashcards").document(document.id).delete()
                }
                Toast.makeText(this, "Topic deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete topic", Toast.LENGTH_SHORT).show()
                Log.w("TopicsActivity", "Error deleting topic", e)
            }
    }
}