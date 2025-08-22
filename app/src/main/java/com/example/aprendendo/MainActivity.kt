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
import kotlin.time.Duration
import android.content.Context
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private lateinit var listView: ListView
    private lateinit var btBlue: ImageButton
    private val deviceList = mutableListOf<BluetoothDevice>()
    private var socket: BluetoothSocket? = null
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val REQUEST_PERMISSIONS_CODE = 1
    private val TAG = "MainActivity"




    private val seekValues = mutableMapOf("X" to 0, "Y" to 0, "Z" to 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate iniciado")
        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "Layout carregado")
            checkPermissions()
            Log.d(TAG, "Permissões verificadas")
        } catch (e: Exception) {
            Log.e(TAG, "Erro no onCreate: ${e.message}", e)
            finish()
        }
    }

    private fun checkPermissions() {
        Log.d(TAG, "Verificando permissões")
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: requestCode=$requestCode")
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeControls()
            } else {
                finish()
            }
        }
    }

    private fun initializeControls() {
       // Log.d(TAG, "Inicializando controles")
       // if (bluetoothAdapter == null) {
         //   Log.e(TAG, "Bluetooth não suportado no dispositivo")
       //     finish()
       //     return
       // }

       // if (!bluetoothAdapter!!.isEnabled) {
        //    Log.w(TAG, "Bluetooth não está habilitado")
         //   finish()
        //    return
       // }

        setupSeekBars()
        setupButtons()
        setupDeviceList()
        setupJoystick()
        listView.visibility = View.INVISIBLE
    }

    private fun setupSeekBars() {
        Log.d(TAG, "Configurando SeekBars")
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

                    val mensagemParaEnviarx = "<$valorX>"
                    val mensagemParaEnviary = "<$valorY>"
                    val mensagemParaEnviarz = "<$valorZ>"
                    Log.d(TAG, "$mensagemParaEnviarx")
                    sendBluetoothMessage(mensagemParaEnviarx)
                    Log.d(TAG, "$mensagemParaEnviary")
                    sendBluetoothMessage(mensagemParaEnviary)
                    Log.d(TAG, "$mensagemParaEnviarz")
                    sendBluetoothMessage(mensagemParaEnviarz)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //    val valorX = seekValues["X"] ?: 0
                 //   val valorY = (seekValues["Y"] ?: 0) + 180
                 //   val valorZ = (seekValues["Z"] ?: 0) + 360

                 //  val mensagemParaEnviarx = "<$valorX>"
                 ///   val mensagemParaEnviary = "<$valorY>"
                 //   val mensagemParaEnviarz = "<$valorZ>"
                 //   Log.d(TAG, "$mensagemParaEnviarx")
                 //   sendBluetoothMessage(mensagemParaEnviarx)
                 //   Log.d(TAG, "$mensagemParaEnviary")
                 //   sendBluetoothMessage(mensagemParaEnviary)
                //    Log.d(TAG, "$mensagemParaEnviarz")
                  //  sendBluetoothMessage(mensagemParaEnviarz)
                }
            })
        }
    }


    private fun vib(duration: Long){
        val vibrador = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val efeito = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrador.vibrate(efeito)
        }else{

            vibrador.vibrate(duration)

        }
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


    @SuppressLint("SuspiciousIndentation")
    private fun setupButtons() {
        Log.d(TAG, "Configurando botões")
        val bt_abre = findViewById<ImageButton>(R.id.bt_abre)

        bt_abre.setOnClickListener {
            sendBluetoothMessage("1000")
           // vib(500)
            animarClique(bt_abre)
        }




        val bt_fecha = findViewById<ImageButton>(R.id.bt_fecha)
                    bt_fecha.setOnClickListener {
                     sendBluetoothMessage("1001")
               // vib(500)
                     animarClique(bt_fecha)
            }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setupJoystick() {
    val btnjoy = findViewById<Button>(R.id.joybtn)

        btnjoy.setOnClickListener {
            val intent = Intent(this, Joystick::class.java)
            startActivity(intent)
        }

    }

    private fun setupDeviceList() {
        var abre = false
        Log.d(TAG, "Configurando lista de dispositivos")
        btBlue = findViewById(R.id.bt_blue)
        listView = findViewById(R.id.listadis)


        btBlue.setOnClickListener {
            if(!abre) {
                listView.visibility = View.VISIBLE
                fetchPairedDevices()
                abre = true
                vib(100)
            }
            else{
                listView.visibility = View.INVISIBLE
                abre = false
                vib(100)
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            connectToDevice(deviceList[position])
            listView.visibility = View.INVISIBLE
        }



    }

    private fun fetchPairedDevices() {
        Log.d(TAG, "Buscando dispositivos pareados")
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            return
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }

        val pairedDevices = bluetoothAdapter!!.bondedDevices
        deviceList.clear()

        if (pairedDevices.isNotEmpty()) {
            deviceList.addAll(pairedDevices)
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                deviceList.map { it.name ?: it.address }
            )
            listView.adapter = adapter
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Log.d(TAG, "Conectando ao dispositivo: ${device.name}")
        thread {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    return@thread
                }
                socket?.close()
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                socket?.connect()
            } catch (e: IOException) {
                Log.e(TAG, "Falha na conexão: ${e.message}", e)
            }
        }
    }

    private fun sendBluetoothMessage(message: String) {
        Log.d(TAG, "Enviando mensagem: $message")
        if (socket == null || !socket!!.isConnected) {
            return
        }

        thread {
            try {
                socket?.outputStream?.write(message.toByteArray())
            } catch (e: IOException) {
                Log.e(TAG, "Erro ao enviar mensagem: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy chamado")
        super.onDestroy()
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Erro ao fechar socket: ${e.message}", e)
        }
    }
}