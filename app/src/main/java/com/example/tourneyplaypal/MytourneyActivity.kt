package com.example.tourneyplaypal

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tourneyplaypal.data.GamesEntity
import com.example.tourneyplaypal.data.TournamentEntity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

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
                    if (tournament != null && (tournament.host == currentUserEmail || isUserParticipating(currentUserEmail, tournament))) {
                        addTournamentView(tournamentContainer, tournament)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MytourneyActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isUserParticipating(currentUserEmail: String?, tournament: TournamentEntity): Boolean {
        return tournament.players?.contains(currentUserEmail) ?: false
    }


    @SuppressLint("MissingInflatedId")
    private fun addTournamentView(container: LinearLayout, tournament: TournamentEntity) {
        val inflater = LayoutInflater.from(this)
        val tournamentView = inflater.inflate(R.layout.item_tournament_mytournaments, container, false)

        val tournamentTitle = tournamentView.findViewById<TextView>(R.id.tournamentTitle)
        val tournamentGame = tournamentView.findViewById<TextView>(R.id.tournamentGame)
        val tournamentPlayerCount = tournamentView.findViewById<TextView>(R.id.tournamentPlayerCount)
        val manageButton = tournamentView.findViewById<Button>(R.id.manageButton)
        val tournamentImage = tournamentView.findViewById<ImageView>(R.id.tournamentImage)

        tournamentTitle.text = tournament.name
        tournamentGame.text = tournament.game
        tournamentPlayerCount.text = "${tournament.currentPlayers} / ${tournament.playerCount}"

        val colorRes = gameColors[tournament.game] ?: R.color.default_color
        val imageRes = gameImages[tournament.game] ?: R.drawable.new_icon

        tournamentView.setBackgroundColor(resources.getColor(colorRes, null))
        tournamentImage.setImageResource(imageRes)

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (tournament.host == currentUserEmail) {
            manageButton.text = "Gestionar Torneo"
            manageButton.setOnClickListener {
                showManageTournamentDialog(tournament)
            }

        } else {
            val isUserParticipating = isUserParticipating(currentUserEmail, tournament)
            if (isUserParticipating) {
                manageButton.text = "Abandonar Torneo"
                manageButton.setOnClickListener {
                    showLeaveTournamentDialog(tournament, currentUserEmail!!)
                }
            } else {
                manageButton.visibility = View.GONE
            }
        }

        container.addView(tournamentView)
    }

    private fun showManageTournamentDialog(tournament: TournamentEntity) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Gestionar Torneo")
            .setItems(arrayOf("Seleccionar ganador", "Expulsar jugador", "Cancelar torneo")) { _, which ->
                when (which) {
                    0 -> selectWinner(tournament)
                    1 -> showExpelPlayerDialog(tournament)
                    2 -> cancelAndDeleteTournament(tournament)
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun selectWinner(tournament: TournamentEntity) {
        val participants = tournament.players ?: emptyList()

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Seleccionar ganador")
            .setCancelable(true)
            .setSingleChoiceItems(participants.toTypedArray(), -1) { dialog, which ->
                val selectedWinner = participants[which]
                updateWinnerInFirebase(tournament, selectedWinner)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun updateWinnerInFirebase(tournament: TournamentEntity, winnerEmail: String) {
        val database = FirebaseDatabase.getInstance()
        val tournamentRef = database.getReference("tournaments").child(tournament.id ?: "")

        tournamentRef.child("winner").setValue(winnerEmail)
            .addOnSuccessListener {
                updateUsersHistory(tournament, winnerEmail)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR_OF_DAY, 24)
                val endTimeMillis = calendar.timeInMillis

                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        tournamentRef.removeValue()
                            .addOnFailureListener { e ->
                                Log.e("MytourneyActivity", "Error al eliminar el torneo", e)
                            }
                    }
                }, endTimeMillis)
            }
            .addOnFailureListener { e ->
                Log.e("MytourneyActivity", "Error al seleccionar ganador", e)
            }
    }

    private fun updateUsersHistory(tournament: TournamentEntity, winnerEmail: String) {
        val database = FirebaseDatabase.getInstance().reference

        tournament.players?.forEach { playerEmail ->
            val formattedEmail = playerEmail.replace(".", ",")
            val userHistoryRef = database.child("users").child(formattedEmail).child("history")
            val userStatsRef = database.child("users").child(formattedEmail)

            val historyStatus = if (playerEmail == winnerEmail) "Ganador" else "Jugado"
            userHistoryRef.child(tournament.id!!).setValue(historyStatus)
                .addOnSuccessListener {
                    Log.d("MytourneyActivity", "Historial actualizado para $playerEmail")

                    // Actualizaremos las estadísticas de los usuarios dependiendo de si ganan o no
                    userStatsRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(mutableData: MutableData): Transaction.Result {
                            var participations = mutableData.child("participations").getValue(Int::class.java)
                            if (participations == null) {
                                participations = 0
                            }
                            participations += 1

                            mutableData.child("participations").value = participations

                            if (playerEmail == winnerEmail) {
                                var wins = mutableData.child("wins").getValue(Int::class.java)
                                if (wins == null) {
                                    wins = 0
                                }
                                wins += 1
                                mutableData.child("wins").value = wins
                            }

                            return Transaction.success(mutableData)
                        }

                        override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                            if (databaseError != null) {
                                Log.e("MytourneyActivity", "Error al actualizar estadísticas de $playerEmail", databaseError.toException())
                            } else {
                                Log.d("MytourneyActivity", "Estadísticas actualizadas para $playerEmail")
                            }
                        }
                    })
                }
                .addOnFailureListener { e ->
                    Log.e("MytourneyActivity", "Error al actualizar historial de $playerEmail", e)
                }
        }
    }




    private fun showExpelPlayerDialog(tournament: TournamentEntity) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Expulsar a un jugador")
            .setMessage("¿Estás seguro de que quieres expulsar a un jugador de este torneo?")
            .setCancelable(true)
            .setPositiveButton("Sí") { _, _ ->
                showExpelPlayerSelectionDialog(tournament)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun showExpelPlayerSelectionDialog(tournament: TournamentEntity) {
        val players = tournament.players ?: return
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

        // Filtrar la lista de jugadores para excluir al usuario actual, el host no se puede echar a si mismo
        val filteredPlayers = players.filter { it != currentUserEmail }
        val playerEmails = filteredPlayers.toTypedArray()

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Seleccionar Jugador a Expulsar")
            .setSingleChoiceItems(playerEmails, -1) { dialog, which ->
                val playerToExpel = playerEmails[which]
                expelPlayerFromTournament(tournament, playerToExpel)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun expelPlayerFromTournament(tournament: TournamentEntity, playerToExpel: String) {
        val database = FirebaseDatabase.getInstance()
        val tournamentRef = database.getReference("tournaments").child(tournament.id ?: "")

        tournamentRef.child("players").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.value as? List<String>
                players?.let {
                    val updatedPlayers = it.filter { player -> player != playerToExpel }

                    tournamentRef.child("players").setValue(updatedPlayers)

                    tournamentRef.child("currentPlayers").setValue(updatedPlayers.size)

                    Toast.makeText(this@MytourneyActivity, "Se ha expulsado a $playerToExpel del torneo", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MytourneyActivity", "Error al expulsar al jugador del torneo", error.toException())
                Toast.makeText(this@MytourneyActivity, "Error al expulsar al jugador del torneo", Toast.LENGTH_SHORT).show()
            }
        })
    }





    private fun cancelAndDeleteTournament(tournament: TournamentEntity) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Cancelar y borrar torneo")
            .setMessage("¿Estás seguro de que quieres cancelar y borrar este torneo?")
            .setCancelable(true)
            .setPositiveButton("Sí") { _, _ ->
                val database = FirebaseDatabase.getInstance()
                val tournamentRef = database.getReference("tournaments").child(tournament.id ?: "")

                tournamentRef.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this@MytourneyActivity, "El torneo se ha cancelado y borrado correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MytourneyActivity", "Error al cancelar y borrar el torneo", exception)
                        Toast.makeText(this@MytourneyActivity, "Error al cancelar y borrar el torneo", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun showLeaveTournamentDialog(tournament: TournamentEntity, currentUserEmail: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Abandonar Torneo")
            .setMessage("¿Estás seguro de que quieres abandonar este torneo?")
            .setCancelable(true)
            .setPositiveButton("Sí") { _, _ ->
                leaveTournament(tournament.id!!, currentUserEmail)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }


    private fun leaveTournament(tournamentId: String, userEmail: String) {
        val tournamentRef = FirebaseDatabase.getInstance().getReference("tournaments").child(tournamentId)
        tournamentRef.child("players").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                if (players.contains(userEmail)) {
                    players.remove(userEmail)
                    tournamentRef.child("players").setValue(players)
                    tournamentRef.child("currentPlayers").setValue(players.size)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MyTourneyActivity", "Error al abandonar el torneo", error.toException())
            }
        })
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
                val checkBoxJoinTournament = dialog.findViewById<CheckBox>(R.id.checkBoxJoinTournament)
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
                    val joinTournament = checkBoxJoinTournament.isChecked

                    val tournament = TournamentEntity(
                        name = name,
                        game = game,
                        playerCount = playerCount
                    )

                    if (joinTournament) {
                        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                        currentUserEmail?.let {
                            tournament.players.add(it)
                            tournament.currentPlayers = 1
                        }
                    }

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

        val threeDaysMillis = 3 * 24 * 60 * 60 * 1000
        tournament.endDate = (System.currentTimeMillis() + threeDaysMillis).toString()
        tournament.id = reference.key

        Log.d("MyTourneyActivity", "Tournament Details: $tournament")

        reference.setValue(tournament.toMap())
    }
}
