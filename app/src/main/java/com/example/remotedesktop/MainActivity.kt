package com.example.remotedesktop

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.remotedesktop.databinding.ActivityMainBinding
import com.example.remotedesktop.databinding.IpDialogLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.DataInputStream
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private var socketThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        //full screen
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)

        getIp()
    }

    /**
     * METHOD FOR GETTING IP FROM USER
     */
    private fun getIp() {
        //custom dialog layout
        val dialogBinding = DataBindingUtil.inflate<IpDialogLayoutBinding>(
            LayoutInflater.from(this), R.layout.ip_dialog_layout, null, false
        )
        dialogBinding.ipEditText.setText(viewModel.ip)
        // Create an alert dialog to get ip from user
        MaterialAlertDialogBuilder(this).setTitle("Enter IP").setView(dialogBinding.root)
            .setPositiveButton("Connect") { _, _ ->
                val ip = dialogBinding.ipEditText.text.toString()
                viewModel.ip = ip

                //now start socket connection
                connectToServer()
            }.setNegativeButton("Exit") { _, _ ->
                finish()
            }.setCancelable(false).show()
    }

    /**
     * METHOD FOR CONNECTING TO SERVER
     */
    private fun connectToServer() {
        socketThread = Thread {
            try {
                viewModel.socket = Socket(viewModel.ip, 8080)
                runOnUiThread {
                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
                }

                //start receiving image data
                receiveImage()

            } catch (e: Exception) {
                runOnUiThread {
                    getIp()
                    Toast.makeText(this, "Error Connecting to server", Toast.LENGTH_SHORT).show()
                }
            }
        }
        socketThread!!.start()
    }

    /**
     * METHOD FOR RECEIVING IMAGE DATA FROM SERVER AND DISPLAY ON IMAGE VIEW
     */
    private fun receiveImage() {
        //get data stream
        viewModel.dataInputStream = DataInputStream(viewModel.socket?.getInputStream())
        while (true) {
            try {
                //image size
                viewModel.size = viewModel.dataInputStream?.readInt()!!

                //image bytes
                viewModel.bytes = ByteArray(viewModel.size)
                viewModel.dataInputStream?.readFully(viewModel.bytes)

                //creating bitmap
                val bitmap =
                    BitmapFactory.decodeByteArray(viewModel.bytes, 0, viewModel.bytes!!.size)
                runOnUiThread {
                    //set image on imageview
                    binding.imageView.setImageBitmap(bitmap)
                }
                Thread.sleep(1)
            } catch (e: Exception) {
                runOnUiThread {
                    getIp()
                    Toast.makeText(this, "Server Disconnected", Toast.LENGTH_SHORT).show()
                }
                break;
            }
        }
    }

    /**
     * CLOSE SOCKET CONNECTION AND SOCKET THREAD WHEN USER CLOSES THE APP
     */
    override fun onStop() {
        super.onStop()
        socketThread!!.join()
        viewModel.socket!!.close()
        viewModel.dataInputStream!!.close()
    }

    override fun onStart() {
        super.onStart()
        if (socketThread != null) socketThread!!.start()
    }
}