package com.example.flashcardapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.recyclerview.widget.ItemTouchHelper

// Activity that handles showing the list of flashcards for a certain topic using a RecyclerView
// Also includes search and delete logic
class FlashcardListActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private var allFlashcards: List<Flashcard> = emptyList()
    private lateinit var flashcardAdapter: FlashcardAdapter
    private lateinit var backToTopicsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard_list)
        recyclerView = findViewById(R.id.flashcardRecyclerView)
        searchBar = findViewById(R.id.searchBar)
        backToTopicsButton = findViewById(R.id.backToTopicsButton)

        fetchFlashcards()

        backToTopicsButton.setOnClickListener {
            startActivity(Intent(this, TopicListActivity::class.java))
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFlashcards(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        enableSwipeToDelete()
    }

    private fun fetchFlashcards() {
        val selectedTopic = intent.getStringExtra("SELECTED_TOPIC") ?: ""

        val db = FirebaseFirestore.getInstance()
        db.collection("user_flashcards")
            .whereEqualTo("topic", selectedTopic)
            .get()
            .addOnSuccessListener { documents ->
                allFlashcards = documents.mapNotNull { document ->
                    document.toObject(Flashcard::class.java)
                }
                setupRecyclerView(allFlashcards, selectedTopic) // Pass selectedTopic here
            }
            .addOnFailureListener { e ->
                Log.w("FlashcardListActivity", "Error fetching flashcards", e)
                Toast.makeText(this, "Failed to load flashcards", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRecyclerView(flashcards: List<Flashcard>, selectedTopic: String) {
        flashcardAdapter = FlashcardAdapter(flashcards) { flashcard ->
            val intent = Intent(this, UserFlashcardsActivity::class.java).apply {
                putExtra("QUESTION", flashcard.question)
                putExtra("ANSWER", flashcard.answer)
                putExtra("TOPIC", selectedTopic) // Pass the selected topic here
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = flashcardAdapter
    }


    private fun filterFlashcards(query: String) {
        val filteredFlashcards = allFlashcards.filter { flashcard ->
            flashcard.question.contains(query, ignoreCase = true) ||
                    flashcard.answer.contains(query, ignoreCase = true)
        }
        flashcardAdapter.updateData(filteredFlashcards)
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
                    val removedFlashcard = flashcardAdapter.removeItem(position)
                    deleteFlashcardFromDatabase(removedFlashcard)
                }
            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun deleteFlashcardFromDatabase(flashcard: Flashcard) {
        val db = FirebaseFirestore.getInstance()
        db.collection("user_flashcards")
            .whereEqualTo("question", flashcard.question)
            .whereEqualTo("answer", flashcard.answer)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("user_flashcards").document(document.id).delete()
                }
                Toast.makeText(this, "Flashcard deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete flashcard", Toast.LENGTH_SHORT).show()
                Log.w("FlashcardListActivity", "Error deleting flashcard", e)
            }
    }
}