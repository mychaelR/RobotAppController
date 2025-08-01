package com.example.aprendendo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import android.widget.*

class Joystick : AppCompatActivity() {

    private val joy1: SeekBar by lazy { findViewById(R.id.joy1) }
    private val joy2: SeekBar by lazy { findViewById(R.id.joy2) }
    private val bta: ImageView by lazy { findViewById(R.id.bta) }
    private val btf: ImageView by lazy { findViewById(R.id.btf) }
    private val custom: Button by lazy { findViewById(R.id.custom) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.joystick)
        setupJoystick()
        setupButtons()
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

    fun animarClique(view: View, escala: Float = 1.3f, duracao: Long = 20) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, escala)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, escala)
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", escala, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", escala, 1f)

        val scaleUp = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            duration = duracao
            interpolator = android.view.animation.OvershootInterpolator()
        }

        val scaleDown = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY)
            duration = duracao
            startDelay = duracao
            interpolator = android.view.animation.OvershootInterpolator()
        }

        AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
            start()
        }
    }

    private fun setupButtons() {
            val bta = findViewById<ImageView>(R.id.bta)
            val btf = findViewById<ImageView>(R.id.btf)
        bta.setOnClickListener {
            animarClique(bta)
    }
        btf.setOnClickListener {
            animarClique(btf)
        }

    }




}
