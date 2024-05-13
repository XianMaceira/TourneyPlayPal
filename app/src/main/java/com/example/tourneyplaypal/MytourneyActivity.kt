package com.example.tourneyplaypal

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.tourneyplaypal.data.TournamentEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MytourneyActivity : AppCompatActivity() {

    private val games = arrayOf("CS2", "Fortnite", "League of Legends", "PUBG", "Brawl Stars")
    private val playerCounts = arrayOf("4", "8", "16")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mytournaments)

        showMyTourneys()
    }

    private fun showMyTourneys() {
        title = "Mis Torneos"

        val backHomeFromMytourneysButton = findViewById<Button>(R.id.backHomeFromMytourneysButton)
        val createTournamentButton = findViewById<FloatingActionButton>(R.id.createTournamentFloatingActionButton)

        backHomeFromMytourneysButton.setOnClickListener {
            finish()
        }

        createTournamentButton.setOnClickListener {
            showCreateTournamentDialog()
        }
    }

    private fun showCreateTournamentDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.create_tournament_dialog)

        val editTextTournamentName = dialog.findViewById<EditText>(R.id.editTextTournamentName)
        val spinnerPlayerCount = dialog.findViewById<Spinner>(R.id.spinnerPlayerCount)
        val spinnerGame = dialog.findViewById<Spinner>(R.id.spinnerGame)
        val buttonCreateTournament = dialog.findViewById<Button>(R.id.buttonCreateTournament)

        val playerCountAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, playerCounts)
        playerCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPlayerCount.adapter = playerCountAdapter

        val gameAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, games)
        gameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGame.adapter = gameAdapter

        buttonCreateTournament.setOnClickListener {
            val name = editTextTournamentName.text.toString()
            val playerCount = spinnerPlayerCount.selectedItem.toString().toInt()
            val game = spinnerGame.selectedItem.toString()

            val tournament = TournamentEntity(
                name = name,
                game = game,
                playerCount = playerCount
            )

            saveTournamentToFirebase(tournament)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveTournamentToFirebase(tournament: TournamentEntity) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("tournaments").push()

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        tournament.host = currentUserEmail

        tournament.winner = null

        tournament.startDate = System.currentTimeMillis().toString()

        val threeDaysMillis = 3 * 24 * 60 * 60 * 1000 // 3 días en milisegundos
        tournament.endDate = (System.currentTimeMillis() + threeDaysMillis).toString()

        // Establecer la ID del torneo como la clave generada por Firebase
        tournament.id = reference.key

        Log.d("MyTourneyActivity", "Tournament Details: $tournament")


        reference.setValue(tournament.toMap())

    }
}
