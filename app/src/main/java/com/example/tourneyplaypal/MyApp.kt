package com.example.tourneyplaypal

import android.app.Application
import android.widget.Toast
import com.example.tourneyplaypal.data.addPredefinedGames
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                addPredefinedGames()
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(this@MyApp, "El servidor no está disponible", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

