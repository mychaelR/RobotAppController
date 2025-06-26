package com.example.aprendendo
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private lateinit var listView: ListView
    private lateinit var btBlue: ImageButton // Corrigido de Button para ImageButton
    private val deviceList = mutableListOf<BluetoothDevice>()
    private var socket: BluetoothSocket? = null
    private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val REQUEST_PERMISSIONS_CODE = 1
    private val TAG = "MainActivity"

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
           // showToast("Erro ao iniciar: ${e.message}")
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
                //showToast("Permissões necessárias não concedidas")
                finish()
            }
        }
    }

    private fun initializeControls() {
        Log.d(TAG, "Inicializando controles")
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth não suportado no dispositivo")
           // showToast("Bluetooth não suportado")
            finish()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            Log.w(TAG, "Bluetooth não está habilitado")
            //showToast("Ative o Bluetooth")
            finish()
            return
        }

        setupSeekBars()
        setupButtons()
        setupDeviceList()
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
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    textView.text = progress.toString()
                    sendBluetoothMessage("$axis:$progress")
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun setupButtons() {
        Log.d(TAG, "Configurando botões")
        findViewById<ImageButton>(R.id.bt_abre).setOnClickListener { sendBluetoothMessage("ABRE") }
        findViewById<ImageButton>(R.id.bt_fecha).setOnClickListener { sendBluetoothMessage("FECHA") }
    }

    private fun setupDeviceList() {
        Log.d(TAG, "Configurando lista de dispositivos")
        btBlue = findViewById(R.id.bt_blue)
        listView = findViewById(R.id.listViewDispositivos)

        btBlue.setOnClickListener {
            listView.visibility = View.VISIBLE
            fetchPairedDevices()

        }

        listView.setOnItemClickListener { _, _, position, _ ->
            connectToDevice(deviceList[position])
            listView.visibility = View.INVISIBLE
        }
    }

    private fun fetchPairedDevices() {
        Log.d(TAG, "Buscando dispositivos pareados")
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
           // showToast("Ative o Bluetooth")
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
        } else {
            //showToast("Nenhum dispositivo pareado")
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Log.d(TAG, "Conectando ao dispositivo: ${device.name}")
        thread {
            try {
                socket?.close()
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                socket?.connect()
               // runOnUiThread {
                    //showToast("Conectado a ${device.name}")
              //  }
            } catch (e: IOException) {
                Log.e(TAG, "Falha na conexão: ${e.message}", e)
                //runOnUiThread {
                    //showToast("Falha na conexão: ${e.message}")
               // }
            }
        }
    }

    private fun sendBluetoothMessage(message: String) {
        Log.d(TAG, "Enviando mensagem: $message")
        if (socket == null || !socket!!.isConnected) {
            //showToast("Não conectado a nenhum dispositivo")
           return
       }

      thread {
            try {
                socket?.outputStream?.write(message.toByteArray())
            } catch (e: IOException) {
               Log.e(TAG, "Erro ao enviar mensagem: ${e.message}", e)
               // runOnUiThread {
                    //showToast("Erro ao enviar mensagem")
               // }
            }
        }
    }

   // private fun showToast(message: String) {
    //    runOnUiThread {
    //        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    //    }
   // }

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