package com.example.tourneyplaypal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RecordActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var tournamentsDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val recordContainer = findViewById<LinearLayout>(R.id.recordLinearLayout)
        val backHomeButton = findViewById<Button>(R.id.backHomeFromRecordButton)
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email?.replace(".", ",")

        backHomeButton.setOnClickListener {
            finish()
        }

        if (currentUserEmail != null) {
            database = FirebaseDatabase.getInstance("https://xmb-tourneyplaypal-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users/$currentUserEmail/history")
            tournamentsDatabase = FirebaseDatabase.getInstance("https://xmb-tourneyplaypal-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("tournaments")

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val recordStatus = dataSnapshot.getValue(String::class.java)
                            val tournamentId = dataSnapshot.key
                            if (recordStatus != null && tournamentId != null) {
                                tournamentsDatabase.child(tournamentId).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(tournamentSnapshot: DataSnapshot) {
                                        val tournamentName = tournamentSnapshot.child("name").getValue(String::class.java)
                                        if (tournamentName != null) {
                                            val textView = TextView(this@RecordActivity)
                                            textView.text = "Torneo: $tournamentName - Estado: $recordStatus"
                                            recordContainer.addView(textView)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@RecordActivity, "Error al obtener el nombre del torneo: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }
                    } else {
                        Toast.makeText(this@RecordActivity, "No hay historial de torneos disponible.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RecordActivity, "Error al cargar el historial de torneos: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Error: No se pudo obtener el correo electrónico del usuario actual.", Toast.LENGTH_SHORT).show()
        }
    }
}
