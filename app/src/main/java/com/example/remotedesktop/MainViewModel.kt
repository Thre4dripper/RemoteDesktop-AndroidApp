package com.example.remotedesktop

import androidx.lifecycle.ViewModel
import java.io.DataInputStream
import java.net.Socket

class MainViewModel : ViewModel() {
    var ip = ""
    var socket: Socket? = null
    var DataInputStream: DataInputStream? = null
}