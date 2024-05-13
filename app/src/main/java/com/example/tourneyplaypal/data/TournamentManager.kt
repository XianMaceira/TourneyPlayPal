package com.example.tourneyplaypal.data

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TournamentManager {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val tournamentRef: DatabaseReference = database.getReference("tournaments")

    fun addTournament(tournament: TournamentEntity) {
        val key = tournamentRef.push().key
        key?.let {
            tournament.id = it
            tournamentRef.child(it).setValue(tournament.toMap())
        }
    }
}
