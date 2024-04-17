package com.example.ko_bluetooth.network

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ko_bluetooth.MainActivity

class BluetoothService(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    //블루투스 사용가능기기 확인
    fun isBluetoothSupported() = bluetoothAdapter != null

    @RequiresApi(Build.VERSION_CODES.S)
    fun requestPermissionsIfNeeded() {
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, MainActivity.REQUEST_ACCESS_LOCATION)
        requestPermission(Manifest.permission.BLUETOOTH_SCAN, MainActivity.REQUEST_ENABLE_BLUETOOTH)
        requestPermission(Manifest.permission.BLUETOOTH_CONNECT, MainActivity.REQUEST_BLUETOOTH_CONNECT)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            Log.d("requestPermission", "블루투스 권한 : 확인 안됨")
            ActivityCompat.requestPermissions(context as MainActivity, arrayOf(permission), requestCode)
        } else {
            if (requestCode == MainActivity.REQUEST_ENABLE_BLUETOOTH) {
                startBluetoothDeviceDiscovery()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startBluetoothDeviceDiscovery() {
        if (bluetoothAdapter?.isEnabled != true) {
            // 블루투스 활성화 요청
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            (context as Activity).startActivityForResult(enableBtIntent, MainActivity.REQUEST_ENABLE_BT)
            showToast("블루투스를 활성화해주세요.")
        } else {
            // 블루투스 스캔 권한 확인
            val permissionCheck = ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
            Log.d("BluetoothService", "블루투스 스캔 권한 상태: $permissionCheck")
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter?.startDiscovery()
                showToast("기기를 검색합니다.")
            } else {
                // 필요한 경우 권한 요청
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    MainActivity.REQUEST_BLUETOOTH_SCAN_PERMISSION
                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    fun connectToDevice(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            if (device.createBond()) {
                showToast("${device.name} (MAC: ${device.address})와 페어링을 시도합니다.")
            } else {
                showToast("페어링 시도 실패")
            }
        } else {
            ActivityCompat.requestPermissions(
                context as MainActivity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                MainActivity.REQUEST_BLUETOOTH_CONNECT
            )
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

}
