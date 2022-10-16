package com.example.remotedesktop

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)

        connectToServer()
    }

    private fun connectToServer() {
        socketThread = Thread {
            try {
                viewModel.socket = Socket("192.168.0.110", 8080)
                runOnUiThread {
                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
                }

                receiveImage()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        socketThread.start()
    }

    private fun receiveImage() {
        viewModel.dataInputStream = DataInputStream(viewModel.socket?.getInputStream())
        while (true) {
            viewModel.size = viewModel.dataInputStream?.readInt()!!
            viewModel.bytes = ByteArray(viewModel.size)
            viewModel.dataInputStream?.readFully(viewModel.bytes)
            val bitmap = BitmapFactory.decodeByteArray(viewModel.bytes, 0, viewModel.bytes!!.size)
            runOnUiThread {
                binding.imageView.setImageBitmap(bitmap)
            }
            Thread.sleep(1)
        }
    }

    override fun onStop() {
        super.onStop()
        socketThread.join()
        viewModel.socket!!.close()
        viewModel.dataInputStream!!.close()
    }

    override fun onStart() {
        super.onStart()
        connectToServer()
    }
}