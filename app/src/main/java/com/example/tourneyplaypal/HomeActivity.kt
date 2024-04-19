package com.example.tourneyplaypal

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
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
        val logoutButton = findViewById<TextView>(R.id.logoutButton)

        emailTextView.text = email

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            onBackPressedDispatcher
            finish()
        }
    }
}