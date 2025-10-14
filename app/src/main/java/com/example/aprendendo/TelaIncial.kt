package com.example.aprendendo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aprendendo.R.layout.tela_inicial
import com.google.android.material.button.MaterialButton

class TelaIncial : AppCompatActivity() {

 override fun onCreate(savedInstanceState: Bundle?) {
     super.onCreate(savedInstanceState)
    setContentView(tela_inicial)
     setupButtons()

}

    private fun setupButtons() {

        val botao_dpad = findViewById<MaterialButton>(R.id.bt_dpad)
        val botao_axis = findViewById<MaterialButton>(R.id.bt_axis)
        val botao_juntas = findViewById<MaterialButton>(R.id.bt_juntas)
        val botao_config = findViewById<MaterialButton>(R.id.bt_config)

       botao_juntas.setOnClickListener{
           val intent = Intent(this@TelaIncial, JuntasActivity::class.java)
            startActivity(intent)
        }

        botao_dpad.setOnClickListener{
            val intent = Intent(this@TelaIncial, JoystickActivity::class.java)
            startActivity(intent)
        }

        botao_axis.setOnClickListener{
            val intent = Intent(this@TelaIncial, AxisActivity::class.java)
            startActivity(intent)

        }


    }




}