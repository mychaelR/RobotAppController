package com.example.aprendendo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator

import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity
import com.example.aprendendo.R.layout.joystick

class JoystickActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(joystick)

        setupJoystick()
        setupButtons()
    }

    private fun setupJoystick() {
        val joystick1 = findViewById<SeekBar>(R.id.joy1)
        val joystick2 = findViewById<SeekBar>(R.id.joy2)



        joystick1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
               if(fromUser) {
                   val valorjoyHoriz = progress
                   Log.d("JOYSTICK1", "<$valorjoyHoriz>")
                   BluetoothService.sendMessage("<$valorjoyHoriz>")
                   println("<$valorjoyHoriz>")
               }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress = 50

            }
        })

        joystick2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val valorjoyVertz1 = 460 - progress
                    val valorFinal = (progress.toDouble() * 1.8) + 180
                    val valorjoyVertz2 = Math.round(valorFinal).toInt()

                    Log.d("JOYSTICK2", "<$valorjoyVertz1>")
                    BluetoothService.sendMessage("<$valorjoyVertz1>")
                    BluetoothService.sendMessage("<$valorjoyVertz2>")
                    println("z<$valorjoyVertz1>")
                    println("y<$valorjoyVertz2>")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress = 50

            }
        })
    }

    private fun setupButtons() {
        val btfecha = findViewById<ImageView>(R.id.btf)
        btfecha.setOnClickListener {
            BluetoothService.sendMessage("<1000>")

        }
        val btabre = findViewById<ImageView>(R.id.bta)
        btabre.setOnClickListener {
            BluetoothService.sendMessage("<1001>")
    }

}}


