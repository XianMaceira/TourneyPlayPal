package com.example.tourneyplaypal

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


import com.google.firebase.FirebaseApp


class AuthActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        // SPLASH
        Thread.sleep(2000)

        setTheme(R.style.Theme_TourneyPlayPal)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        //AUTENTICACION
        auth()

    }

    private fun auth() {
        title = "Autenticacion"

        FirebaseApp.initializeApp(this)


        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)


        registerButton.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(emailEditText.text.toString(),
                        passwordEditText.text.toString()).addOnCompleteListener {

                        if (it.isSuccessful) {
                            showHome(it.result?.user?.email ?:"")
                        } else {
                            showAlertMessage()
                        }
                    }
            }
        }

        loginButton.setOnClickListener {
            if (emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()) {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(emailEditText.text.toString(),
                        passwordEditText.text.toString()).addOnCompleteListener {

                        if (it.isSuccessful) {
                            showHome(it.result?.user?.email ?:"")
                        } else {
                            showAlertMessage()
                        }
                    }
            }
        }

    }

    private fun showAlertMessage() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error, si el usuario ya existe inicie sesion, si no existe compruebe sus datos")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(homeIntent)
    }
}