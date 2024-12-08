package com.example.flashcardapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter to maintain the list of flashcards saved to a certain topic
class FlashcardAdapter(
    private var flashcards: List<Flashcard>,
    private val onClick: (Flashcard) -> Unit
) : RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    class FlashcardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val questionText: TextView = view.findViewById(R.id.flashcardText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.flashcard_item, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        val flashcard = flashcards[position]
        holder.questionText.text = flashcard.question
        holder.itemView.setOnClickListener {
            onClick(flashcard)
        }
    }

    override fun getItemCount(): Int = flashcards.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFlashcards: List<Flashcard>) {
        flashcards = newFlashcards
        notifyDataSetChanged()
    }

    fun removeItem(position: Int): Flashcard {
        val removedFlashcard = flashcards[position]
        flashcards = flashcards.toMutableList().apply { removeAt(position) }
        notifyItemRemoved(position)
        return removedFlashcard
    }
}