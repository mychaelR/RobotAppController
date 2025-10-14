package com.example.aprendendo

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.IOException
import java.util.UUID
import kotlin.concurrent.thread

class AxisActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val deviceList = mutableListOf<BluetoothDevice>()
    private var socket: BluetoothSocket? = null
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val REQUEST_PERMISSIONS_CODE = 1
    private val TAG = "AxisActivity"

    private val seekValues = mutableMapOf("X" to 0, "Y" to 0, "Z" to 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.axis_activity)
        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = buildPermissionsList()
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                REQUEST_PERMISSIONS_CODE
            )
        } else {
            initializeControls()
        }
    }

    @SuppressLint("InlinedApi")
    private fun buildPermissionsList(): List<String> {
        return buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.BLUETOOTH_SCAN)
                }
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.BLUETOOTH_CONNECT)
                }
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun alert() {
        // MUDANÇA: Usando MaterialAlertDialogBuilder para um visual consistente
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("CONFIGURAÇÕES")
            .setItems(arrayOf("Dispositivos bluetooth", "Dev mode")) { _, which ->
                when (which) {
                    0 -> {
                        fetchPairedDevices()
                        if (deviceList.isNotEmpty()) {
                            val bluetoothDialog = MaterialAlertDialogBuilder(this)
                                .setTitle("BLUETOOTH")
                                .setItems(deviceList.map { it.name ?: it.address }.toTypedArray()) { _, which2 ->
                                    val selectedDevice = deviceList[which2]
                                    connectToDevice(selectedDevice)
                                    Toast.makeText(this, "Conectando a ${selectedDevice.name ?: selectedDevice.address}", Toast.LENGTH_SHORT).show()
                                }
                                .create()
                            bluetoothDialog.show()
                        } else {
                            Toast.makeText(this, "Nenhum dispositivo pareado encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        builder.create().show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeControls()
            } else {
                Toast.makeText(this, "Permissões necessárias para usar o app", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun initializeControls() {
        setupSeekBars()
        setupButtons()
        setupDeviceList()
        setupJoystick()
    }


    private fun setupSeekBars() {
        val seekBars = listOf(
            Triple(R.id.barx, R.id.grausx, "X"),
            Triple(R.id.bary, R.id.grausy, "Y"),
            Triple(R.id.barz, R.id.grausz, "Z")
        )

        seekBars.forEach { (barId, textId, axis) ->
            val seekBar = findViewById<android.widget.SeekBar>(barId) // <-- SeekBar
            val textView = findViewById<TextView>(textId)

            val initialProgress = seekBar.progress
            val initialValue = when (axis) {
                "Y" -> initialProgress
                "Z" -> initialProgress
                else -> initialProgress
            }
            textView.text = initialValue.toString()
            seekValues[axis] = initialProgress

            seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    seekValues[axis] = progress
                    val displayValue = when (axis) {
                        "Y" -> progress
                        "Z" -> progress
                        else -> progress
                    }
                    textView.text = displayValue.toString()
                    val valorFinalX = seekValues["X"] ?: 0
                    val valorFinalY = (seekValues["Y"] ?: 0)
                    val valorFinalZ = (seekValues["Z"] ?: 0)
                    BluetoothService.sendMessage("<$valorFinalX>")
                    BluetoothService.sendMessage("<$valorFinalY>")
                    BluetoothService.sendMessage("<$valorFinalZ>")

                }
                override fun onStartTrackingTouch(s: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(s: android.widget.SeekBar?) {}
            })
        }
    }

    private fun setupButtons() {
        
        val btAbre = findViewById<MaterialButton>(R.id.bt_abre)
        btAbre.setOnClickListener {
            BluetoothService.sendMessage("<1000>")
            animarClique(btAbre)
        }

        val btFecha = findViewById<MaterialButton>(R.id.bt_fecha)
        btFecha.setOnClickListener {
            BluetoothService.sendMessage("<1001>")
            animarClique(btFecha)
        }
    }

    private fun setupJoystick() {
        val btnjoy = findViewById<MaterialButton>(R.id.joybtn)
        btnjoy.setOnClickListener {
            val intent = Intent(this@AxisActivity, JoystickActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDeviceList() {
        val btBlue = findViewById<MaterialButton>(R.id.bt_blue)
        btBlue.setOnClickListener { alert() }
    }

    private fun fetchPairedDevices() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }
        val pairedDevices = bluetoothAdapter!!.bondedDevices
        deviceList.clear()
        deviceList.addAll(pairedDevices)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        thread {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    return@thread
                }
                socket?.close()
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                socket?.connect()

                BluetoothService.socket = socket
                runOnUiThread {
                    Toast.makeText(this, "Conectado a ${device.name}", Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, "Conectado a ${device.name}")
            } catch (e: IOException) {
                runOnUiThread {
                    Toast.makeText(this, "Falha na conexão", Toast.LENGTH_SHORT).show()
                }
                Log.e(TAG, "Falha na conexão: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao fechar socket: ${e.message}", e)
        }
    }

  //  private fun vib(duration: Long) {
    //    val vibrador = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
   //         val efeito = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
    //        vibrador.vibrate(efeito)
     //   } else {
     //       @Suppress("DEPRECATION")
    //        vibrador.vibrate(duration)
    //    }
   // }

    // Nenhuma mudança necessária aqui
    fun animarClique(view: View, escala: Float = 1.3f, duracao: Long = 20) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, escala)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, escala)
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", escala, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", escala, 1f)

        val scaleUp = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            this.duration = duracao
            interpolator = OvershootInterpolator()
        }

        val scaleDown = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY)
            this.duration = duracao
            startDelay = duracao
            interpolator = OvershootInterpolator()
        }

        AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
            start()
        }
    }
}