package com.example.tourneyplaypal

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class AuthActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // SPLASH
        Thread.sleep(2000)

        setTheme(R.style.Theme_TourneyPlayPal)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        firebaseAuth = FirebaseAuth.getInstance()

        initUI()
    }

    private fun initUI() {
        title = "Autenticacion"

        FirebaseApp.initializeApp(this)

        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            if (user != null && !user.isEmailVerified) {
                                sendEmailVerification()
                                showAlertMessage("Registrado con exito. Se ha enviado un correo electrónico de verificación.")
                            } else {
                                showAlertMessage("Error en el registro, inténtalo de nuevo.")
                            }
                        } else {
                            showAlertMessage("Error en el registro. Inténtalo de nuevo. Si ya tienes una cuenta verifica tu correo e inicia sesión.")
                        }
                    }
            } else {
                showAlertMessage("Por favor, complete todos los campos.")
            }
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            if (user != null && user.isEmailVerified) {
                                showHome(email)
                            } else {
                                showAlertMessage("Por favor, verifica tu correo electrónico antes de iniciar sesión.")
                            }
                        } else {
                            showAlertMessage("Por favor, comprueba los datos introducidos. Si no tienes una cuenta de TourneyPlayPal, regístrate primero.")
                        }
                    }
            } else {
                showAlertMessage("Por favor, completa todos los campos.")
            }
        }
    }

    private fun showAlertMessage(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Mensaje")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun sendEmailVerification() {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Correo de verificación enviado.", Toast.LENGTH_SHORT).show()
            } else {
                showAlertMessage("Error al enviar el correo de verificación.")
            }
        }
    }

    private fun showHome(email: String) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(homeIntent)
        finish()
    }
}




/*package com.example.tourneyplaypal

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

    private fun showVerifyMessage() {


    }

    private fun showHome(email: String) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(homeIntent)
    }
}*/