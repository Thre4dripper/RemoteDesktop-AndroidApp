package com.example.remotedesktop

import androidx.lifecycle.ViewModel
import java.io.DataInputStream
import java.net.Socket

class MainViewModel : ViewModel() {
    var ip = ""
    var socket: Socket? = null
    var dataInputStream: DataInputStream? = null
    var size = 0
    var bytes: ByteArray? = null
}