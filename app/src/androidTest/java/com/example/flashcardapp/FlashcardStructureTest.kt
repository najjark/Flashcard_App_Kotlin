package com.example.flashcardapp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Test to validate the structure of flashcards
class FlashcardStructureTest {

    @Test
    fun testValidateFlashcardInput() {
        val topic = "Math"
        val question = "What is 2+2?"
        val answer = "4"

        val isValid = validateFlashcardInput(topic, question, answer)

        assertTrue(isValid)
    }

    @Test
    fun testValidateFlashcardInput_with_emptyFields() {
        val topic = ""
        val question = "What is 2+2?"
        val answer = "4"

        val isValid = validateFlashcardInput(topic, question, answer)

        assertFalse(isValid)
    }

    private fun validateFlashcardInput(topic: String, question: String, answer: String): Boolean {
        return topic.isNotEmpty() && question.isNotEmpty() && answer.isNotEmpty()
    }
}