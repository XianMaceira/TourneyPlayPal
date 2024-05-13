package com.example.tourneyplaypal.data

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TournamentEntity(
    @get:Exclude
    var id: String? = null,
    var name: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var winner: String? = null,
    var host: String? = null,
    var game: String? = null,
    var playerCount: Int = 0
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "startDate" to startDate,
            "endDate" to endDate,
            "host" to host,
            "game" to game,
            "winner" to winner,
            "playerCount" to playerCount
        )
    }
}
