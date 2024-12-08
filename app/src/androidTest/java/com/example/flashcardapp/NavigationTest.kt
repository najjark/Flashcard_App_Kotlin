package com.example.flashcardapp

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.action.ViewActions.click
import org.junit.After
import org.junit.Before
import org.junit.Test

// Class to test navigation to different parts of the app
class NavigationTest {

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun teardown() {
        Intents.release()
    }

    @Test
    fun testNavigateToCreateFlashcardActivity() {
        ActivityScenario.launch(HomePageActivity::class.java)

        onView(withId(R.id.createFlashcardButton)).check(matches(isDisplayed()))

        onView(withId(R.id.createFlashcardButton)).perform(click())

        Intents.intended(hasComponent(CreateFlashcardActivity::class.java.name))
    }

    @Test
    fun testNavigateToLoginPage() {
        ActivityScenario.launch(HomePageActivity::class.java)

        onView(withId(R.id.logoutButton)).check(matches(isDisplayed()))

        onView(withId(R.id.logoutButton)).perform(click())

        Intents.intended(hasComponent(LoginActivity::class.java.name))
    }
}