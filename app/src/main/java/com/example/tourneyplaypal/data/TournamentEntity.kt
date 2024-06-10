package com.example.tourneyplaypal.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TournamentEntity(
    var id: String? = null,
    var name: String? = null,
    var game: String? = null,
    var playerCount: Int = 0,
    var currentPlayers: Int = 0,
    var host: String? = null,
    var winner: String? = null,
    var startDate: String? = null,
    var isFull: Boolean? = null,
    var players: MutableList<String> = mutableListOf(),
    var ended: Boolean = false,
    var endDate: String? = null,
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "game" to game,
            "playerCount" to playerCount,
            "currentPlayers" to currentPlayers,
            "host" to host,
            "winner" to winner,
            "startDate" to startDate,
            "isFull" to isFull,
            "players" to players,
            "ended" to ended,
            "endDate" to endDate
        )
    }
}
