# Kotlin Flashcard App

# Flashcard Creation
This Kotlin app allows users to create, review, and automatically generate flashcards using a LLM.

The user can manually enter the questions and answers to create their own flashcards so they can be used later.

Alternatively the user can enter a topic, and specify the number of flashcards to be generated, and the application will send an API call to the LLM to create the questions and answers.

The user can then save these flashcards into their collection for later use.

# Progress Tracking
The application will keep track of the user's most recent topics and the percentage of correctly answered flashcards for that topic.

# Firebase Storage and Authentication
The app uses a Firestore database to store the flashcards and whether they were answered correctly or not so the user can review their progress.
It also implements Authentication using Firebase to keep track of user accounts and progress.
