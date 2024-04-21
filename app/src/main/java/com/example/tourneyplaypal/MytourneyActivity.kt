package com.example.tourneyplaypal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MytourneyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mytournaments)

        showMyTourneys()
    }

    private fun showMyTourneys() {

        title = "Mis Torneos"

        val backHomeFromMytourneysButton = findViewById<Button>(R.id.backHomeFromMytourneysButton)



        backHomeFromMytourneysButton.setOnClickListener {

            val GoBackIntent = Intent(this, HomeActivity::class.java).apply {

            }
            startActivity(GoBackIntent)

        }

    }
}