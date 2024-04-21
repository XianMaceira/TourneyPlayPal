package com.example.tourneyplaypal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity()  {

    override fun onCreate(savedInstaceState: Bundle?) {
        super.onCreate(savedInstaceState)
        setContentView(R.layout.activity_home)

        //SHOW PROFILE
        val bundle = intent.extras
        val email = bundle?.getString("email")
        showprofile(email ?:"")
    }


    private fun showprofile(email: String) {

        title = "Perfil"

        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val goToMyTourneysButton = findViewById<Button>(R.id.mytourneysButton)
        val goToExploreButton = findViewById<Button>(R.id.exploreButton)


        emailTextView.text = email

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            onBackPressedDispatcher
            finish()
        }
        goToMyTourneysButton.setOnClickListener {

            val MyTourneysIntent = Intent(this, MytourneyActivity::class.java).apply {

            }
            startActivity(MyTourneysIntent)

        }

        goToExploreButton.setOnClickListener {

            val ExploreIntent = Intent(this, ExploreActivity::class.java).apply {

            }
            startActivity(ExploreIntent)

        }

    }
}