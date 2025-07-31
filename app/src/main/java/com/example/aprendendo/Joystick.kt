package com.example.aprendendo

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import android.widget.*

class Joystick : AppCompatActivity() {

    private val joy1: SeekBar by lazy { findViewById(R.id.joy1) }
    private val joy2: SeekBar by lazy { findViewById(R.id.joy2) }
    private val bta: Button by lazy { findViewById(R.id.bta) }
    private val btf: Button by lazy { findViewById(R.id.btf) }
    private val custom: Button by lazy { findViewById(R.id.custom) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.joystick)
        setupJoystick()

    }

    private fun setupJoystick() {

        val joystick1 = findViewById<SeekBar>(R.id.joy1)
        val joystick2 = findViewById<SeekBar>(R.id.joy2)

        joystick1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                seekBar?.progress = 50
            }
        })

        joystick2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                seekBar?.progress = 50
            }
        })




    }
}
