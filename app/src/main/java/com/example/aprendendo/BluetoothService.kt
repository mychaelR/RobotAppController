package com.example.aprendendo


import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import kotlin.concurrent.thread

object BluetoothService {
    var socket: BluetoothSocket? = null

    fun sendMessage(message: String) {
        val s = socket
        if (s == null || !s.isConnected) {
            Log.e("BluetoothService", "Socket não conectado")
            return
        }

        thread {
            try {
                s.outputStream.write(message.toByteArray())
                Log.d("BluetoothService", "Mensagem enviada: $message")
            } catch (e: IOException) {
                Log.e("BluetoothService", "Erro ao enviar: ${e.message}", e)
            }
        }
    }
}
