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
        Thread {
            try {
                viewModel.socket = Socket("192.168.0.110", 8080)
                runOnUiThread {
                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
                }
                viewModel.DataInputStream = DataInputStream(viewModel.socket?.getInputStream())
                while (true) {
                    val size = viewModel.DataInputStream?.readInt()
                    val bytes = ByteArray(size!!)
                    viewModel.DataInputStream?.readFully(bytes)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    runOnUiThread {
                        binding.imageView.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}