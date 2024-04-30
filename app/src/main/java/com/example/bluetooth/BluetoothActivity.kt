package com.example.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothActivity {

    private val TAG = "BluetoothCommActivity"
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

     fun ConnectToDevice(device: BluetoothDevice) {
        // 尝试连接设备
        // 这里只是一个示例，你需要处理异常和错误情况
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID，根据实际情况修改
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        bluetoothSocket?.connect()

        // 获取输入输出流
        outputStream = bluetoothSocket?.getOutputStream()
        inputStream = bluetoothSocket?.getInputStream()

        // 连接成功后的处理
        Log.d(TAG, "Connected to device")

        // 在这里开始监听接收到的数据
        listenForIncomingData()
    }

    fun SendData(data: String) {
        // 发送数据到蓝牙设备
        // 注意：这里只是一个示例，你需要确保outputStream不为null
        outputStream?.write(data.toByteArray())
        Log.d(TAG, "Sent data: $data")
    }

     fun ListenForIncomingData() {
        // 在一个单独的线程中监听数据
        Thread {
            val buffer = ByteArray(1024)
            var bytes: Int

            // 保持循环以持续监听数据
            while (true) {
                try {
                    // 读取输入流中的数据
                    bytes = inputStream?.read(buffer) ?: break
                    val incomingMessage = String(buffer, 0, bytes)
                    Log.d(TAG, "Received data: $incomingMessage")

                    // 在UI线程中更新UI
                    Handler(Looper.getMainLooper()).post {
                        // 显示接收到的数据，比如通过Toast或TextView
                        Toast.makeText(this@BluetoothCommunicationActivity, incomingMessage, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }.start()
    }

}