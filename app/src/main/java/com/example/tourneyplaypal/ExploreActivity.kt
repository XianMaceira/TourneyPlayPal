package com.example.tourneyplaypal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ExploreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exploretournaments)

        showExplore()
    }

    private fun showExplore() {

        title = "Explorar"

        val backHomeFromExploreButton = findViewById<Button>(R.id.backHomeFromExploreButton)



        backHomeFromExploreButton.setOnClickListener {

            val GoBackIntent = Intent(this, HomeActivity::class.java).apply {

            }
            startActivity(GoBackIntent)

        }

    }
}