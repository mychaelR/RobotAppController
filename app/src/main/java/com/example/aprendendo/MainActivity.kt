package com.example.aprendendo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID
import kotlin.concurrent.thread
import android.content.Context
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.view.animation.OvershootInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private lateinit var btBlue: ImageButton
    private val deviceList = mutableListOf<BluetoothDevice>()
    private var socket: BluetoothSocket? = null
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val REQUEST_PERMISSIONS_CODE = 1
    private val TAG = "MainActivity"

    private val seekValues = mutableMapOf("X" to 0, "Y" to 0, "Z" to 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun alert() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("CONFIGURAÇÕES")
            .setItems(arrayOf("Dispositivos bluetooth", "Dev mode")) { _, which ->
                when (which) {
                    0 -> {
                        fetchPairedDevices()
                        if (deviceList.isNotEmpty()) {
                            val bluetoothDialog = AlertDialog.Builder(this)
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
            val seekBar = findViewById<SeekBar>(barId)
            val textView = findViewById<TextView>(textId)

            textView.text = seekBar.progress.toString()
            seekValues[axis] = seekBar.progress

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    textView.text = progress.toString()
                    seekValues[axis] = progress
                    val valorX = seekValues["X"] ?: 0
                    val valorY = (seekValues["Y"] ?: 0) + 180
                    val valorZ = (seekValues["Z"] ?: 0) + 360

                    BluetoothService.sendMessage("<$valorX>")
                    BluetoothService.sendMessage("<$valorY>")
                    BluetoothService.sendMessage("<$valorZ>")

                    println("<$valorY>")

                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun setupButtons() {
        val bt_abre = findViewById<ImageButton>(R.id.bt_abre)
        bt_abre.setOnClickListener {
            BluetoothService.sendMessage("<1000>")
            animarClique(bt_abre)
        }

        val bt_fecha = findViewById<ImageButton>(R.id.bt_fecha)
        bt_fecha.setOnClickListener {
            BluetoothService.sendMessage("<1001>")
            animarClique(bt_fecha)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setupJoystick() {
        val btnjoy = findViewById<Button>(R.id.joybtn)
        btnjoy.setOnClickListener {
            val intent = Intent(this@MainActivity, JoystickActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDeviceList() {
        btBlue = findViewById(R.id.bt_blue)
        btBlue.setOnClickListener { alert() }
    }

    private fun fetchPairedDevices() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }

        val pairedDevices = bluetoothAdapter!!.bondedDevices
        deviceList.clear()
        if (pairedDevices.isNotEmpty()) {
            deviceList.addAll(pairedDevices)
        }
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

                // 🔹 Agora salva no Singleton
                BluetoothService.socket = socket
                Log.d(TAG, "Conectado a ${device.name}")
            } catch (e: IOException) {
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

    private fun vib(duration: Long) {
        val vibrador = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val efeito = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrador.vibrate(efeito)
        } else {
            vibrador.vibrate(duration)
        }
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    fun animarClique(view: View, escala: Float = 1.3f, duracao: Long = 20) {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, escala)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, escala)
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", escala, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", escala, 1f)

        val scaleUp = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            duration = duracao
            interpolator = OvershootInterpolator()
        }

        val scaleDown = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY)
            duration = duracao
            startDelay = duracao
            interpolator = OvershootInterpolator()
        }

        AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
            start()
        }
    }
}
