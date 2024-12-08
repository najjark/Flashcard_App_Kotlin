package com.example.flashcardapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter to maintain the list of flashcard topics
class TopicAdapter(
    private var topics: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    class TopicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val topicName: TextView = view.findViewById(R.id.topicName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.topic_item, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        holder.topicName.text = topic
        holder.itemView.setOnClickListener { onClick(topic) }
    }

    fun removeItem(position: Int): String {
        val removedTopic = topics[position]
        topics = topics.toMutableList().apply { removeAt(position) }
        notifyItemRemoved(position)
        return removedTopic
    }

    override fun getItemCount(): Int = topics.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(matchedTopics: List<String>) {
        topics = matchedTopics
        notifyDataSetChanged()
    }
}