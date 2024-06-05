package com.example.tourneyplaypal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tourneyplaypal.data.TournamentEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ExploreActivity : AppCompatActivity() {

    private lateinit var tournamentContainer: LinearLayout
    private lateinit var switchCS2: Switch
    private lateinit var switchR6: Switch
    private lateinit var switchRL: Switch
    private lateinit var switchFortnite: Switch
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val gameColors = mapOf(
        "CS2" to R.color.color_cs2,
        "Fortnite" to R.color.color_fortnite,
        "Rainbow Six" to R.color.color_r6,
        "Rocket League" to R.color.color_rocket
    )

    private val gameImages = mapOf(
        "CS2" to R.drawable.cs2_logo,
        "Fortnite" to R.drawable.fortnite_logo,
        "Rainbow Six" to R.drawable.logo_r6,
        "Rocket League" to R.drawable.rocket_logo
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exploretournaments)

        tournamentContainer = findViewById(R.id.tournamentContainer)
        switchCS2 = findViewById(R.id.switchCS2)
        switchR6 = findViewById(R.id.switchR6)
        switchRL = findViewById(R.id.switchRL)
        switchFortnite = findViewById(R.id.switchFortnite)

        database = FirebaseDatabase.getInstance().reference.child("tournaments")
        auth = FirebaseAuth.getInstance()

        switchCS2.setOnCheckedChangeListener { _, _ -> loadTournaments() }
        switchR6.setOnCheckedChangeListener { _, _ -> loadTournaments() }
        switchRL.setOnCheckedChangeListener { _, _ -> loadTournaments() }
        switchFortnite.setOnCheckedChangeListener { _, _ -> loadTournaments() }

        loadTournaments()

        val backHomeFromExploreButton = findViewById<Button>(R.id.backHomeFromExploreButton)
        backHomeFromExploreButton.setOnClickListener {
            finish()
        }
    }

    private fun loadTournaments() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tournamentContainer.removeAllViews()
                for (tournamentSnapshot in snapshot.children) {
                    val tournament = tournamentSnapshot.getValue(TournamentEntity::class.java)
                    if (tournament != null && isGameSelected(tournament.game)) {
                        addTournamentView(tournament)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ExploreActivity", "Error al cargar los torneos", error.toException())
            }
        })
    }

    private fun isGameSelected(game: String?): Boolean {
        return when (game) {
            "CS2" -> switchCS2.isChecked
            "Rainbow Six" -> switchR6.isChecked
            "Rocket League" -> switchRL.isChecked
            "Fortnite" -> switchFortnite.isChecked
            else -> false
        }
    }

    private fun addTournamentView(tournament: TournamentEntity) {
        val inflater = LayoutInflater.from(this)
        val tournamentView = inflater.inflate(R.layout.item_tournament_exploretournaments, tournamentContainer, false)

        val tournamentTitle = tournamentView.findViewById<TextView>(R.id.tournamentTitle)
        val tournamentGame = tournamentView.findViewById<TextView>(R.id.tournamentGame)
        val tournamentPlayerCount = tournamentView.findViewById<TextView>(R.id.tournamentPlayerCount)
        val tournamentImage = tournamentView.findViewById<ImageView>(R.id.tournamentImage)
        val joinButton = tournamentView.findViewById<Button>(R.id.manageButton)

        tournamentTitle.text = tournament.name
        tournamentGame.text = tournament.game
        tournamentPlayerCount.text = "${tournament.currentPlayers}/${tournament.playerCount} jugadores"

        val colorRes = gameColors[tournament.game] ?: R.color.default_color
        tournamentView.setBackgroundColor(resources.getColor(colorRes, null))

        val imageRes = gameImages[tournament.game] ?: R.drawable.ic_launcher_foreground
        tournamentImage.setImageResource(imageRes)

        joinButton.setOnClickListener {
            showJoinConfirmationDialog(tournament)
        }

        tournamentContainer.addView(tournamentView)
    }

    private fun showJoinConfirmationDialog(tournament: TournamentEntity) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("¿Quieres unirte a este torneo?")
            .setCancelable(false)
            .setPositiveButton("Sí") { _, _ ->
                addUserToTournament(tournament)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Unirse al torneo")
        alert.show()
    }

    private fun addUserToTournament(tournament: TournamentEntity) {
        val currentUserEmail = auth.currentUser?.email
        val activityContext = this@ExploreActivity

        if (currentUserEmail != null) {
            val tournamentId = tournament.id
            if (tournamentId != null) {
                val tournamentRef = database.child(tournamentId)
                tournamentRef.child("players").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val players = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                        if (!players.contains(currentUserEmail)) {
                            players.add(currentUserEmail)
                            tournamentRef.child("players").setValue(players)
                            tournamentRef.child("currentPlayers").setValue(tournament.currentPlayers + 1)
                        } else {
                            val dialogBuilder = AlertDialog.Builder(activityContext)
                            dialogBuilder.setMessage("Ya eres miembro de este torneo")
                                .setCancelable(false)
                                .setPositiveButton("Aceptar") { dialog, _ ->
                                    dialog.dismiss()
                                }

                            val alert = dialogBuilder.create()
                            alert.setTitle("Error")
                            alert.show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ExploreActivity", "Error al añadir usuario al torneo", error.toException())
                    }
                })
            } else {
                Toast.makeText(this, "El ID del torneo falta.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
