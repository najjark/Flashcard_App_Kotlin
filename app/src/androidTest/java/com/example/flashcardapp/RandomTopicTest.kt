package com.example.flashcardapp

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

// Class to test the logic for fetching a random topic
class RandomTopicTest {

    @Test
    fun testRandomTopicSelection() {
        val predefinedTopics = RandomTopics.getPredefinedTopics()
        val randomTopic = RandomTopics.selectRandomTopic()

        assertTrue("The selected topic should be one of the predefined topics.",
            predefinedTopics.contains(randomTopic.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }))
    }
}