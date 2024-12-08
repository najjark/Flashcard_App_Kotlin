package com.example.flashcardapp

// List of topics that will be used for random topic flashcards
object RandomTopics {
    private val predefinedTopics = listOf(
        "Science", "History", "Geography", "Math", "Literature", "Technology", "Music", "Art", "Biology",
        "Physics", "Chemistry", "Astronomy", "Philosophy", "Psychology", "Economics", "Sociology",
        "Political Science", "Environmental Studies", "Engineering", "Programming", "Artificial Intelligence",
        "Robotics", "Data Science", "Ethics", "World Religions", "Mythology", "Architecture", "Photography",
        "Film", "Poetry", "World War II", "Renaissance", "Space Exploration", "Nutrition", "Fitness",
        "Medical Science", "Entrepreneurship", "Marketing", "Finance", "Cryptocurrency", "Social Media",
        "Climate Change", "Ecology", "Globalization", "Cultural Studies", "Psychological Disorders",
        "Linguistics", "Languages of the World", "Quantum Physics", "Classical Music", "Sports History"
    )

    fun selectRandomTopic(): String {
        return predefinedTopics.random().lowercase()
    }

    fun getPredefinedTopics(): List<String> = predefinedTopics
}