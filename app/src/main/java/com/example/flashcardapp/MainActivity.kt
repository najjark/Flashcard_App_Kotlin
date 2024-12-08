package com.example.flashcardapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

// Main activity launches the app and navigates to the login activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
