package com.example.aprendendo

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.codertainment.dpadview.DPadView
import com.example.aprendendo.R.layout.joystick
import com.google.android.material.button.MaterialButton

class JoystickActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(joystick)

        setupJoystick()
        setupButtons()
    }


    private fun setupJoystick() {



        val dpad = findViewById<DPadView>(R.id.dpad)

        //checa a direção do dpad para poder saber que dados mandar para o esp32
        dpad.onDirectionPressListener = { direction, action ->
            if(direction == DPadView.Direction.UP){



            }
            if(direction == DPadView.Direction.DOWN){


            }

        }


        val joystick2 = findViewById<SeekBar>(R.id.joy2)
        joystick2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val valorjoyVertz1 = 460 - progress
                    val valorFinal = (progress.toDouble() * 1.8) + 180
                    val valorjoyVertz2 = valorjoyVertz1 + 50

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
        val btfecha = findViewById<MaterialButton>(R.id.btf)
        btfecha.setOnClickListener {
            BluetoothService.sendMessage("<1000>")
        }
        val btabre = findViewById<MaterialButton>(R.id.bta)
        btabre.setOnClickListener {
            BluetoothService.sendMessage("<1001>")
        }
    }
}