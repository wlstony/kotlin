package com.example.bluetooth

class Scanner {

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

    // 蓝牙扫描
//    private val scanCallback = object : ScanCallback() {
//        override fun onScanResult(callbackType: Int, result: ScanResult) {
//            super.onScanResult(callbackType, result)
//            val device = result.device
//            // 将设备添加到列表中
//            addToDeviceList(device)
//        }
//
//        override fun onScanFailed(errorCode: Int) {
//            super.onScanFailed(errorCode)
//            Log.d(blueDebug, "扫描失败$errorCode")
//        }
//    }
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
}