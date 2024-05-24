package com.example.tourneyplaypal.data

import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class GamesEntity(
    val id: String = "",
    val name: String = "",
    val genre: String = "",
    val platform: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "genre" to genre,
            "platform" to platform
        )
    }

    companion object {
        suspend fun saveGame(game: GamesEntity) {
            val database = FirebaseDatabase.getInstance("https://xmb-tourneyplaypal-default-rtdb.europe-west1.firebasedatabase.app/")
            val gamesRef = database.getReference("games")
            val gameId = gamesRef.push().key ?: return
            val gameWithId = game.copy(id = gameId)
            gamesRef.child(gameId).setValue(gameWithId).await()
        }

        suspend fun getAllGames(): List<GamesEntity> {
            val database = FirebaseDatabase.getInstance("https://xmb-tourneyplaypal-default-rtdb.europe-west1.firebasedatabase.app/")
            val gamesRef = database.getReference("games")
            val snapshot = gamesRef.get().await()
            return snapshot.children.mapNotNull { it.getValue(GamesEntity::class.java) }
        }
    }
}


suspend fun addPredefinedGames() {
    val predefinedGames = listOf(
        GamesEntity(name = "Fortnite", genre = "Battle Royale", platform = "Multi-platform"),
        GamesEntity(name = "CS2", genre = "First-Person Shooter", platform = "PC"),
        GamesEntity(name = "Rainbow Six", genre = "First-Person Shooter", platform = "Multi-platform"),
        GamesEntity(name = "Rocket League", genre = "Sports", platform = "Multi-platform")
    )

    val existingGames = GamesEntity.getAllGames().map { it.name }.toSet()

    predefinedGames.forEach { game ->
        if (game.name !in existingGames) {
            GamesEntity.saveGame(game)
        }
    }
}
