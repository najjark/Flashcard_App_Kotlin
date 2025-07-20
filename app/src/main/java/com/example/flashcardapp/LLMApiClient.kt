package com.example.flashcardapp

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull

// Class to handle the call to the LLM to generate the content for the flashcards
class LLMApiClient {
    private val client = OkHttpClient()
    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    private val apiKey = "YOUR_API_KEY"

    // Function to initiate the LLM call, as well as ensuring that new content generated will be different
    // to content already existing in the database
    fun generateFlashcards(
        topic: String,
        numberOfCards: Int,
        callback: (List<Pair<String, String>>) -> Unit,
        errorCallback: (String) -> Unit
    ) {
        fetchExistingFlashcards(topic, { existingFlashcards ->
            val existingFlashcardText = existingFlashcards.joinToString("\n") { (question, answer) ->
                "Question: $question\nAnswer: $answer"
            }

            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Here are the existing flashcards for the topic: $topic, $existingFlashcardText" +
                            "Generate $numberOfCards flashcards for the topic: $topic.\n" +
                            "Each flashcard should have a 'Question' and 'Answer' section. Format like:\n" +
                            "Question: [Your Question Here]\n" +
                            "Answer: [Your Answer Here]" +
                            "Make sure that the new flashcard questions and answers are different to the existing ones" +
                            "Do not include any additional text or formatting. Keep the answers concise, as in no more than 50 characters")
                })
            }

            val requestBody = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", messages)
            }.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("LLMApiClient", "Network error: ${e.message}")
                    errorCallback("Network error: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            response.body?.let { body ->
                                val responseString = body.string()
                                Log.i("API Response", "Raw Response: $responseString")
                                parseApiResponse(responseString, callback, errorCallback)
                            } ?: errorCallback("Empty response body")
                        } else {
                            errorCallback("API error: ${response.message}")
                        }
                    } catch (e: Exception) {
                        Log.e("LLMApiClient", "Error processing response: ${e.message}")
                        errorCallback("Error processing response: ${e.message}")
                    }
                }
            })
        }, errorCallback)
    }

    // Parsing the response from the LLM to ensure it is in a "Question" and "Answer" format
    private fun parseApiResponse(
        responseString: String,
        callback: (List<Pair<String, String>>) -> Unit,
        errorCallback: (String) -> Unit
    ) {
        try {
            val flashcards = mutableListOf<Pair<String, String>>()
            Log.i("Response", "Response string: $responseString")

            val jsonObject = JSONObject(responseString)
            val choices = jsonObject.getJSONArray("choices")

            if (choices.length() > 0) {
                val generatedText = choices.getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                Log.i("Generated Text", "Generated text: $generatedText")

                val flashcardPattern = Regex("(?<=\\n|^)Question:\\s*(.+?)\\s*Answer:\\s*(.+?)(?=\\nQuestion:|\\z)", RegexOption.DOT_MATCHES_ALL)
                val matches = flashcardPattern.findAll(generatedText)

                for (match in matches) {
                    val question = match.groupValues[1].trim()
                    val answer = match.groupValues[2].trim()
                    flashcards.add(Pair(question, answer))
                }

                if (flashcards.isNotEmpty()) {
                    Log.i("Parsed Flashcards", "Parsed flashcards: $flashcards")
                    callback(flashcards)
                } else {
                    errorCallback("No valid flashcards were generated. Check the response format.")
                }
            } else {
                errorCallback("API response contains no 'choices'.")
            }
        } catch (e: JSONException) {
            errorCallback("Parsing error. Response: $responseString. Error: ${e.message}")
        }
    }

    // Function to fetch flashcards in the database for a certain topic so they can be passed to the LLM
    // to ensure it generates unique content
    private fun fetchExistingFlashcards(
        topic: String,
        callback: (List<Pair<String, String>>) -> Unit,
        errorCallback: (String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("llm_flashcard")
            .whereEqualTo("topic", topic.lowercase())
            .get()
            .addOnSuccessListener { querySnapshot ->
                val flashcards = querySnapshot.documents.mapNotNull { document ->
                    val question = document.getString("question")
                    val answer = document.getString("answer")
                    if (question != null && answer != null) Pair(question, answer) else null
                }
                callback(flashcards)
            }
            .addOnFailureListener { exception ->
                Log.e("LLMApiClient", "Error fetching flashcards: ${exception.message}")
                errorCallback("Error fetching flashcards: ${exception.message}")
            }
    }
}
