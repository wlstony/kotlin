package com.example.demoapp

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.Manifest.permission
import android.annotation.SuppressLint
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.HashMap
import java.util.UUID


class MainActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // 经典蓝牙
    private fun startDiscovery() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(foundReceiver, filter)

        // 还可以注册其他扫描相关的广播，如扫描完成等
        val scanFinishedFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(scanFinishedReceiver, scanFinishedFilter)

        if (bluetoothAdapter.isDiscovering == false) {
            bluetoothAdapter.startDiscovery()
        }
    }

    private val foundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 从Intent中获取设备信息
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device != null) {
                    addToDeviceList(device)
                } else {
                    Log.d(blueDebug,"device is null")
                }

            }
        }
    }

    private val scanFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // 扫描完成，可以取消注册广播接收器或执行其他操作
            unregisterReceiver(foundReceiver)

            // 这里可以更新UI表示扫描已完成
            Log.d(blueDebug,"Scan finished")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 确保在Activity销毁时取消注册广播接收器
        unregisterReceiver(foundReceiver)
        unregisterReceiver(scanFinishedReceiver)
    }

    // 处理蓝牙开启请求的回调（如果需要的话）
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // 用户同意开启蓝牙
                startDiscovery()
            } else {
                // 用户拒绝开启蓝牙
                // 显示错误消息或执行其他操作
            }
        }
    }

    // REQUEST_ENABLE_BT 是你定义的请求码，用于在 onActivityResult 中识别请求
    private val REQUEST_ENABLE_BT = 1
    // 经典蓝牙end

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationPermissionRequestCode = 100
    private val bluetoothPermissionScan = 101 // 自定义的请求码
    private val bluetoothPermissionConnect = 102 // 自定义的请求码

    // 蓝牙扫描
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            // 将设备添加到列表中
            addToDeviceList(device)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(blueDebug, "扫描失败$errorCode")
        }
    }
    private var deviceList: MutableList<BluetoothDevice> = mutableListOf()
    private val deviceAdapter = DeviceAdapter(deviceList)
    private val androidVersion = getAndroidVersion()
    // 蓝牙连接发送指令
    private val blueDebug = "blue_debug"

//    private fun startScan() {
//        Log.d(blueDebug, "start scan")
//        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
//        if (bluetoothLeScanner == null) {
//            Toast.makeText(this, "scanner is null, open bluetooth?", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val scanSettings = ScanSettings.Builder()
//            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//            .build()
//
//        val filters = emptyList<ScanFilter>() // 如果有需要，可以添加过滤条件
//
//        // 扫描权限
//        if (androidVersion > 12 &&
//            ActivityCompat.checkSelfPermission(
//                this,
//                permission.BLUETOOTH_SCAN
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // 请求权限
//            ActivityCompat.requestPermissions(this,
//                arrayOf(permission.BLUETOOTH_SCAN),
//                bluetoothPermissionScan)
//            return
//        }
//        // 连接权限
//        if ( androidVersion > 12 && ActivityCompat.checkSelfPermission(
//                this,
//                permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // 请求权限
//            ActivityCompat.requestPermissions(this,
//                arrayOf(permission.BLUETOOTH_CONNECT),
//                bluetoothPermissionConnect)
//            return
//        }
//        // 还需要定位权限
//        // java.lang.SecurityException: Need ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get scan results
//        if ( androidVersion > 6 && ActivityCompat.checkSelfPermission(
//                this,
//                permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // 请求权限
//            ActivityCompat.requestPermissions(this,
//                arrayOf(permission.ACCESS_FINE_LOCATION),
//                locationPermissionRequestCode
//                )
//            return
//        }
//        bluetoothAdapter.startDiscovery()
//        bluetoothLeScanner.startScan(filters, scanSettings, scanCallback)
//    }


    private fun getAndroidVersion(): Int {
        val sdkVersion = Build.VERSION.SDK_INT
        val map = HashMap<Int, Int>()
        map[Build.VERSION_CODES.O_MR1] = 8
        map[Build.VERSION_CODES.O] = 8 // android 8.1
        map[Build.VERSION_CODES.P] = 9
        map[Build.VERSION_CODES.Q] = 10
        map[Build.VERSION_CODES.R] = 11
        map[Build.VERSION_CODES.S] = 12

        return map[sdkVersion] ?:100
    }

//    private fun stopScan() {
//        Log.d(blueDebug, "执行指令,停止扫描")
//        if (androidVersion > 12 && ActivityCompat.checkSelfPermission(
//                this,
//                permission.BLUETOOTH_SCAN
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//
//            return
//        }
//        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
//    }

    private fun addToDeviceList(device: BluetoothDevice) {
        if (!deviceList.contains(device)) {
            Log.d(blueDebug, "add device " + device.name +","  + device.alias + ","+ device.address )
            deviceList.add(device)
            deviceAdapter.notifyItemInserted(deviceList.size - 1)
        }
    }



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

        val exeBtn: Button = findViewById(R.id.execButton)
        val cmdText :EditText = findViewById(R.id.commandText)
        exeBtn.setOnClickListener{
            Toast.makeText(this, "执行:" + cmdText.text.toString(), Toast.LENGTH_SHORT).show()

            val device: BluetoothDevice = deviceList[0]
            connectToDevice(device)
            val cmd: String = cmdText.text.toString() + "\n"
            sendData(cmd)
        }

        Log.d(blueDebug, "load scanner")

        // 假设你有一个RecyclerView来显示设备列表
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = deviceAdapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "蓝牙adapter为空,设备似乎不支持蓝牙", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            bluetoothAdapter.enable()
        }

        // 手动扫描蓝牙
        val selectBtn: Button = findViewById(R.id.scanBlooth)
        selectBtn.setOnClickListener{
            startDiscovery()
        }


    }

    private fun connectToDevice(device: BluetoothDevice) {
        // 尝试连接设备
        // 这里只是一个示例，你需要处理异常和错误情况
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID，根据实际情况修改
        if (androidVersion > 12 && ActivityCompat.checkSelfPermission(
                this,
                permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 请求权限
            ActivityCompat.requestPermissions(this,
                arrayOf(permission.BLUETOOTH_CONNECT),
                bluetoothPermissionConnect)
            Log.d(blueDebug, "connectToDevice lack of BLUETOOTH_CONNECT")
            return
        }
        Log.d(blueDebug, "try to connect " + device.address )

        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

        bluetoothSocket?.let { socket ->
            try {
                // 尝试连接，如果bluetoothSocket不为null，connect()方法将被调用
                socket.connect()

                Log.d(blueDebug,"Bluetooth socket connected successfully.")
                // 获取输入输出流
                outputStream = bluetoothSocket?.outputStream
                inputStream = bluetoothSocket?.inputStream

                // 连接成功后的处理
                Log.d(blueDebug, "listenForIncomingData")
                // 在这里开始监听接收到的数据
                listenForIncomingData()
            } catch (e: IOException) {
                Toast.makeText(this, "Failed to connect to Bluetooth socket: ${e.message}", Toast.LENGTH_SHORT).show()

                // 捕获并处理IOException，这是connect()方法可能抛出的异常类型
                Log.d(blueDebug,"Failed to connect to Bluetooth socket: ${e.message}")
                e.printStackTrace() // 可选，用于在日志中打印完整的堆栈跟踪
            }
        } ?: run {
            Toast.makeText(this, "Bluetooth socket is null, cannot connect.", Toast.LENGTH_SHORT).show()
            Log.d(blueDebug,"Bluetooth socket is null, cannot connect.")
        }
    }

    private fun sendData(data: String) {
        Log.d(blueDebug, "Sent data: $data")
        if(outputStream == null) {
            Toast.makeText(this, "Sent data: $data, outputStream is null", Toast.LENGTH_SHORT).show()
            Log.d(blueDebug,"sendData output is null")
        } else {
            outputStream?.write(data.toByteArray())

        }
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
                    Log.d(blueDebug, "Received data: $incomingMessage")

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

//    override fun onDestroy() {
//        super.onDestroy()
//        // 关闭连接
//        try {
//            inputStream?.close()
//            outputStream?.close()
//            bluetoothSocket?.close()
//        } catch (e: IOException){
//
//        }
//    }

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
        if (requestCode == bluetoothPermissionScan) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "BlueTooth scan permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }
}

class DeviceAdapter(private val devices: List<BluetoothDevice>) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {
    private val blueDebug = "blue_debug"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(blueDebug,"onBindViewHolder")
        val device = devices[position]
        holder.deviceName.text = "name:$device.name,alias:$device.alias,address:$device.address"
    }

    override fun getItemCount(): Int = devices.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.tv_item)
        // 如果有其他视图，可以在这里初始化它们
    }

}