package com.example.remotedesktop

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.remotedesktop.databinding.ActivityMainBinding
import java.io.DataInputStream
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var socketThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        //full screen
        window.decorView.systemUiVisibility =
            (android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN)

        connectToServer()
    }

    private fun connectToServer() {
        socketThread = Thread {
            try {
                viewModel.socket = Socket("192.168.0.110", 8080)
                runOnUiThread {
                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
                }
                viewModel.dataInputStream = DataInputStream(viewModel.socket?.getInputStream())
                while (true) {
                    viewModel.size = viewModel.dataInputStream?.readInt()!!
                    viewModel.bytes = ByteArray(viewModel.size)
                    viewModel.dataInputStream?.readFully(viewModel.bytes)
                    val bitmap =
                        BitmapFactory.decodeByteArray(viewModel.bytes, 0, viewModel.bytes!!.size)
                    runOnUiThread {
                        binding.imageView.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        socketThread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketThread.join()
        viewModel.socket?.close()
    }

    override fun onRestart() {
        super.onRestart()
        connectToServer()
    }
}