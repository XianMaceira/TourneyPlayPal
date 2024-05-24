package com.example.tourneyplaypal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tourneyplaypal.data.TournamentEntity

class TournamentAdapter(
    private val tournaments: List<TournamentEntity>,
    private val onAddClickListener: (TournamentEntity) -> Unit
) : RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder>() {

    class TournamentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tournamentImage: ImageView = itemView.findViewById(R.id.tournamentImage)
        val tournamentTitle: TextView = itemView.findViewById(R.id.tournamentTitle)
        val tournamentGame: TextView = itemView.findViewById(R.id.tournamentGame)
        val tournamentPlayerCount: TextView = itemView.findViewById(R.id.tournamentPlayerCount)
        val addButton: Button = itemView.findViewById(R.id.addButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TournamentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_tournament, parent, false)
        return TournamentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TournamentViewHolder, position: Int) {
        val tournament = tournaments[position]
        holder.tournamentTitle.text = tournament.name
        holder.tournamentGame.text = tournament.game
        holder.tournamentPlayerCount.text = "${tournament.playerCount} / Max"

        // Aquí puedes cargar la imagen del torneo si tienes una URL en TournamentEntity
        // holder.tournamentImage.setImageResource(R.drawable.tu_imagen) // Ejemplo

        holder.addButton.setOnClickListener {
            onAddClickListener(tournament)
        }
    }

    override fun getItemCount() = tournaments.size
}
