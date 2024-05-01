package com.example.demoapp

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.Manifest.permission
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapp.ui.theme.DemoAPPTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationPermissionRequestCode = 100

    // 蓝牙扫描
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            // 将设备添加到列表中
            addToDeviceList(device)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            // 处理扫描失败
        }
    }

    private var deviceList: MutableList<BluetoothDevice> = mutableListOf()
    private val deviceAdapter = DeviceAdapter(deviceList)
    val REQUEST_LOCATION_PERMISSION = 100
    private fun startScan() {
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        if (bluetoothLeScanner == null) {
            Toast.makeText(this, "bluetoothLeScanner is null", Toast.LENGTH_SHORT).show()
            return
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = emptyList<ScanFilter>() // 如果有需要，可以添加过滤条件
        if (ActivityCompat.checkSelfPermission(
                this,
                permission.BLUETOOTH_SCAN
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
        bluetoothLeScanner.startScan(filters, scanSettings, scanCallback)
    }

    private fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                this,
                permission.BLUETOOTH_SCAN
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
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }

    private fun addToDeviceList(device: BluetoothDevice) {
        if (!deviceList.contains(device)) {
            deviceList.add(device)
            deviceAdapter.notifyItemInserted(deviceList.size - 1)
        }
    }


    // 蓝牙连接发送指令
    private val TAG = "BluetoothCommActivity"
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // 发送定位的代码
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val sendLocationBtn: Button = findViewById(R.id.uploadLocation)
        sendLocationBtn.setOnClickListener{
            if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission.ACCESS_FINE_LOCATION),
                    locationPermissionRequestCode
                )
            } else {
                getCurrentLocation()
            }

        }


        // 发送指令
        // 假设你已经通过某种方式选择了设备（比如通过扫描）
        // val device: BluetoothDevice = ...

        // 尝试连接
        // 注意：这里只是示例，你需要自己实现选择设备并进行连接
        // connectToDevice(device)


        val exeBtn: Button = findViewById(R.id.execButton)
        val cmdText :EditText = findViewById(R.id.command)
        exeBtn.setOnClickListener{
            // 发送数据
            sendData(cmdText.text.toString())
        }


        // 手动扫描蓝牙
        val selectBtn: Button = findViewById(R.id.scanBlooth)
        selectBtn.setOnClickListener{
            startScan()
        }

        // 假设你有一个RecyclerView来显示设备列表
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = deviceAdapter

        if (bluetoothAdapter == null) {
            // 蓝牙不可用，显示错误信息
            // ...
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
        }

        // 检查位置权限
        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
        } else {
            startScan()
        }


    }







    private fun connectToDevice(device: BluetoothDevice) {
        // 尝试连接设备
        // 这里只是一个示例，你需要处理异常和错误情况
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID，根据实际情况修改
        if (ActivityCompat.checkSelfPermission(
                this,
                permission.BLUETOOTH_CONNECT
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
        if (ActivityCompat.checkSelfPermission(
                this,
                permission.BLUETOOTH_CONNECT
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
        bluetoothSocket?.connect()

        // 获取输入输出流
        outputStream = bluetoothSocket?.outputStream
        inputStream = bluetoothSocket?.inputStream

        // 连接成功后的处理
        Log.d(TAG, "Connected to device")

        // 在这里开始监听接收到的数据
        listenForIncomingData()
    }

    private fun sendData(data: String) {
        // 发送数据到蓝牙设备
        // 注意：这里只是一个示例，你需要确保outputStream不为null
        outputStream?.write(data.toByteArray())
        Log.d(TAG, "Sent data: $data")
    }

    private fun listenForIncomingData() {
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
                       val textView: TextView = findViewById(R.id.showResponse)
                        textView.text = incomingMessage
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 关闭连接
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException){

        }
    }

    private fun getCurrentLocation() {
        if (isLocationEnabled()) {
            if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission.ACCESS_FINE_LOCATION),
                    locationPermissionRequestCode
                )
            }
            fusedLocationProviderClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                }).addOnSuccessListener(this) { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                location?.let {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Toast.makeText(this, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener(this) { e ->
                // Failed to get location
                e.printStackTrace()
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Please enable location", Toast.LENGTH_LONG).show()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

}

class DeviceAdapter(private val devices: List<BluetoothDevice>) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.activity_main, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        if (ActivityCompat.checkSelfPermission(
                holder.itemView.context,
                permission.BLUETOOTH_CONNECT
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
        holder.deviceName.text = device.name
        // 如果需要显示更多信息，可以添加更多的holder视图并设置它们
    }

    override fun getItemCount(): Int = devices.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.recyclerView)
        // 如果有其他视图，可以在这里初始化它们
    }

}