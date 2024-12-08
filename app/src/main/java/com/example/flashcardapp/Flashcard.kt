package com.example.flashcardapp

// Class that holds necessary information for flashcards
data class Flashcard(
    val question: String  = "",
    val answer: String = "",
    var isAnswered: Boolean = false,
    var isCorrect: Boolean? = null,
    var isSaved: Boolean = false,
    val topic: String = ""
)