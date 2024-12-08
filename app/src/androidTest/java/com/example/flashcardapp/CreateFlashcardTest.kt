package com.example.flashcardapp

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.tasks.await

class CreateFlashcardTest {

    private lateinit var firestore: FirebaseFirestore

    @Before
    fun setup() {
        firestore = FirebaseFirestore.getInstance()
    }

    @After
    fun teardown() {
        // Clean up Firestore after tests
        runBlocking {
            val snapshot = firestore.collection("user_flashcards").get().await()
            snapshot.documents.forEach { it.reference.delete() }
        }
    }

    @Test
    fun testCreateFlashcard() {
        // Launch the activity
        val intent = Intent(Intent.ACTION_MAIN)
        ActivityScenario.launch<CreateFlashcardActivity>(intent)

        // Input text into the fields
        onView(withId(R.id.topicInput)).perform(typeText("Math"), closeSoftKeyboard())
        onView(withId(R.id.questionInput)).perform(typeText("What is 2 + 2?"), closeSoftKeyboard())
        onView(withId(R.id.answerInput)).perform(typeText("4"), closeSoftKeyboard())

        // Click the save button
        onView(withId(R.id.saveButton)).perform(click())

        // Check that a Toast message is displayed
//        onView(withText("Flashcard saved!")).inRoot(ToastMatcher()).check(matches(isDisplayed()))

        // Verify the flashcard was saved in Firestore
        runBlocking {
            val snapshot = firestore.collection("user_flashcards").get().await()
            assert(snapshot.documents.isNotEmpty()) { "No flashcards saved in Firestore." }
            val savedFlashcard = snapshot.documents.first().data!!
            assert(savedFlashcard["topic"] == "math")
            assert(savedFlashcard["question"] == "What is 2 + 2?")
            assert(savedFlashcard["answer"] == "4")
        }
    }
}