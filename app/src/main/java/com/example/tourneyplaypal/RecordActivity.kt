package com.example.tourneyplaypal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Exception

class RecordActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private val TAG = "RecordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        val recordContainer = findViewById<LinearLayout>(R.id.recordContainer)
        val totalParticipationsTextView = findViewById<TextView>(R.id.totalParticipations)
        val totalWinsTextView = findViewById<TextView>(R.id.totalWins)
        val winPercentageTextView = findViewById<TextView>(R.id.winPercentage)
        val backHomeButton = findViewById<Button>(R.id.backHomeFromRecordButton)
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email?.replace(".", ",")

        backHomeButton.setOnClickListener {
            finish()
        }

        if (currentUserEmail != null) {
            database = FirebaseDatabase.getInstance("https://xmb-tourneyplaypal-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users/$currentUserEmail")


            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val participations = snapshot.child("participations").getValue(Int::class.java) ?: 0
                        val wins = snapshot.child("wins").getValue(Int::class.java) ?: 0
                        val winPercentage = if (participations > 0) (wins.toDouble() / participations * 100) else 0.0

                        totalParticipationsTextView.text = "Participaciones: $participations"
                        totalWinsTextView.text = "Victorias: $wins"
                        winPercentageTextView.text = "Índice de Victoria: %.2f%%".format(winPercentage)

                        val historyRef = database.child("history")
                        historyRef.orderByKey().limitToLast(10).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                recordContainer.removeAllViews()
                                for (dataSnapshot in snapshot.children) {
                                    val recordStatus = dataSnapshot.getValue(String::class.java)
                                    val tournamentId = dataSnapshot.key
                                    if (recordStatus != null && tournamentId != null) {
                                        FirebaseDatabase.getInstance("https://xmb-tourneyplaypal-default-rtdb.europe-west1.firebasedatabase.app/")
                                            .getReference("tournaments").child(tournamentId).addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(tournamentSnapshot: DataSnapshot) {
                                                    val tournamentName = tournamentSnapshot.child("name").getValue(String::class.java) ?: "N/A"
                                                    val gameName = tournamentSnapshot.child("game").getValue(String::class.java) ?: "N/A"
                                                    val recordView = LayoutInflater.from(this@RecordActivity).inflate(R.layout.item_tournament_record, recordContainer, false)
                                                    val tournamentTitle = recordView.findViewById<TextView>(R.id.tournamentTitle)
                                                    val tournamentStatus = recordView.findViewById<TextView>(R.id.tournamentStatus)
                                                    val tournamentGameLogo = recordView.findViewById<ImageView>(R.id.tournamentGameLogo)
                                                    val tournamentRecordContainer = recordView.findViewById<LinearLayout>(R.id.tournamentRecordContainer)


                                                    tournamentTitle.text = tournamentName
                                                    tournamentStatus.text = recordStatus
                                                    tournamentStatus.setTextColor(
                                                        if (recordStatus == "Ganador") resources.getColor(R.color.color_win)
                                                        else resources.getColor(R.color.color_played)
                                                    )


                                                    val (gameLogo, backgroundColor) = getGameLogoAndColor(gameName)
                                                    tournamentGameLogo.setImageResource(gameLogo)
                                                    tournamentRecordContainer.setBackgroundColor(resources.getColor(backgroundColor))

                                                    recordContainer.addView(recordView)
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    showErrorToast("Error al cargar los detalles del torneo: ${error.message}")
                                                    Log.e(TAG, "Error al cargar los detalles del torneo", error.toException())
                                                }
                                            })
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                showErrorToast("Error al cargar el historial: ${error.message}")
                                Log.e(TAG, "Error al cargar el historial", error.toException())
                            }
                        })
                    } catch (e: Exception) {
                        showErrorToast("Error al procesar los datos del usuario")
                        Log.e(TAG, "Error al procesar los datos del usuario", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showErrorToast("Error al cargar las estadísticas del usuario: ${error.message}")
                    Log.e(TAG, "Error al cargar las estadísticas del usuario", error.toException())
                }
            })
        } else {
            showErrorToast("No se encontró el usuario actual")
            Log.e(TAG, "No se encontró el usuario actual")
        }
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun getGameLogoAndColor(gameName: String): Pair<Int, Int> {
        return when (gameName) {
            "Fortnite" -> Pair(R.drawable.fortnite_logo, R.color.color_fortnite)
            "CS2" -> Pair(R.drawable.cs2_logo, R.color.color_cs2)
            "Rocket League" -> Pair(R.drawable.rocket_logo, R.color.color_rocket)
            "Rainbow Six" -> Pair(R.drawable.logo_r6, R.color.color_r6 )
            else -> Pair(R.drawable.ic_launcher_foreground, R.color.default_color)
        }
    }
}
