package com.example.aprendendo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aprendendo.databinding.TelaInicialBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TelaIncial : AppCompatActivity() {

    private lateinit var binding: TelaInicialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TelaInicialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCards()
    }

    private fun setupCards() {

        // Referência aos seus cards no XML
        val cardDpad = findViewById<MaterialCardView>(R.id.dpmode)
        val cardJuntas = findViewById<MaterialCardView>(R.id.juntasmode)
        val cardCartesiano = findViewById<MaterialCardView>(R.id.cartesianomode)
        val Bt_blue = findViewById<MaterialButton>(R.id.bt_config)


        // Clique do modo Juntas
        cardJuntas.setOnClickListener {
            val intent = Intent(this@TelaIncial, JuntasActivity::class.java)
            startActivity(intent)
        }

        // Clique do modo dpad
        cardDpad.setOnClickListener {
            val intent = Intent(this@TelaIncial, JoystickActivity::class.java)
            startActivity(intent)
        }

        // Clique do modo Cartesiano
        cardCartesiano.setOnClickListener {
            val intent = Intent(this@TelaIncial, AxisActivity::class.java)
            startActivity(intent)
        }
    }
}
