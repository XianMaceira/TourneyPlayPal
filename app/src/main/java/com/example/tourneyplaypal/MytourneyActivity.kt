package com.example.tourneyplaypal

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tourneyplaypal.data.GamesEntity
import com.example.tourneyplaypal.data.TournamentEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MytourneyActivity : AppCompatActivity() {

    private val playerCounts = arrayOf("4", "8", "16")
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
        setContentView(R.layout.activity_mytournaments)

        showMyTourneys()
    }

    private fun showMyTourneys() {
        title = "Mis Torneos"

        val backHomeFromMytourneysButton = findViewById<Button>(R.id.backHomeFromMytourneysButton)
        val createTournamentButton = findViewById<FloatingActionButton>(R.id.createTournamentFloatingActionButton)
        val tournamentContainer = findViewById<LinearLayout>(R.id.tournamentContainer)

        backHomeFromMytourneysButton.setOnClickListener {
            finish()
        }

        createTournamentButton.setOnClickListener {
            showCreateTournamentDialog()
        }

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        val database = FirebaseDatabase.getInstance("https://xmb-tourneyplaypal-default-rtdb.europe-west1.firebasedatabase.app/")
        val tournamentsRef = database.getReference("tournaments")

        tournamentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tournamentContainer.removeAllViews()
                for (dataSnapshot in snapshot.children) {
                    val tournament = dataSnapshot.getValue(TournamentEntity::class.java)
                    if (tournament != null && tournament.host == currentUserEmail) {
                        addTournamentView(tournamentContainer, tournament)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MytourneyActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addTournamentView(container: LinearLayout, tournament: TournamentEntity) {
        val inflater = LayoutInflater.from(this)
        val tournamentView = inflater.inflate(R.layout.item_tournament, container, false)

        val tournamentTitle = tournamentView.findViewById<TextView>(R.id.tournamentTitle)
        val tournamentGame = tournamentView.findViewById<TextView>(R.id.tournamentGame)
        val tournamentPlayerCount = tournamentView.findViewById<TextView>(R.id.tournamentPlayerCount)
        val addButton = tournamentView.findViewById<Button>(R.id.addButton)
        val tournamentImage = tournamentView.findViewById<ImageView>(R.id.tournamentImage)

        tournamentTitle.text = tournament.name
        tournamentGame.text = tournament.game
        tournamentPlayerCount.text = "${tournament.currentPlayers} / ${tournament.playerCount}"

        val colorRes = gameColors[tournament.game] ?: R.color.default_color
        val imageRes = gameImages[tournament.game] ?: R.drawable.new_icon

        tournamentView.setBackgroundColor(resources.getColor(colorRes, null))
        tournamentImage.setImageResource(imageRes)

        addButton.setOnClickListener {
            Toast.makeText(this, "Añadido a ${tournament.name}", Toast.LENGTH_SHORT).show()
        }

        container.addView(tournamentView)
    }

    private fun showCreateTournamentDialog() {
        fetchGames { games, errorMessage ->
            if (errorMessage != null) {
                showErrorDialog(errorMessage)
            } else {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.create_tournament_dialog)

                val editTextTournamentName = dialog.findViewById<EditText>(R.id.editTextTournamentName)
                val spinnerPlayerCount = dialog.findViewById<Spinner>(R.id.spinnerPlayerCount)
                val spinnerGame = dialog.findViewById<Spinner>(R.id.spinnerGame)
                val buttonCreateTournament = dialog.findViewById<Button>(R.id.buttonCreateTournament)

                val playerCounts = listOf(2, 4, 8, 16)
                val playerCountAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, playerCounts)
                playerCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPlayerCount.adapter = playerCountAdapter

                val gameNames = games?.map { it.name } ?: emptyList()
                val gameAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gameNames)
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
        }
    }

    private fun fetchGames(onGamesFetched: (List<GamesEntity>?, String?) -> Unit) {
        val database = FirebaseDatabase.getInstance("https://xmb-tourneyplaypal-default-rtdb.europe-west1.firebasedatabase.app/")
        val gamesRef = database.getReference("games")
        gamesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val games = mutableListOf<GamesEntity>()
                snapshot.children.forEach { child ->
                    val game = child.getValue(GamesEntity::class.java)
                    game?.let { games.add(it) }
                }
                onGamesFetched(games, null)
            }

            override fun onCancelled(error: DatabaseError) {
                onGamesFetched(null, "El servidor no está disponible.")
            }
        })
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
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
        tournament.id = reference.key

        tournament.currentPlayers = 0

        Log.d("MyTourneyActivity", "Tournament Details: $tournament")

        reference.setValue(tournament.toMap())
    }
}
