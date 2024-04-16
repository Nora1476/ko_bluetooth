package com.example.ko_bluetooth

import android.Manifest

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat



@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_ENABLE_BLUETOOTH = 1
        private const val REQUEST_ACCESS_LOCATION = 2
        private const val REQUEST_BLUETOOTH_CONNECT = 3
    }

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var isReceiverRegistered = false


    //액티비티 생명주기 중 하나로, 액티비티가 생성될 때 호출.
    // 이 메소드 내에서 UI를 설정하고, 초기화 작업을 수행
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        //윈도우 인셋을 적절하게 처리하고 뷰의 패딩을 설정하여 시스템 UI와의 겹침을 방지
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBluetoothAdapter()
        checkNetworkAndBluetooth()

    }//onCreate

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkNetworkAndBluetooth() {
        if (isNetworkAvailable()) {
            showToast("네트워크 연결됨")
        } else {
            showToast("네트워크 연결되지 않음")
        }

        if (isBluetoothSupported()) {
            checkBluetoothState()
            requestPermissionsIfNeeded()
        } else {
            showToast("이 기기는 블루투스를 지원하지 않습니다.")
        }
    }

    //디바이스의 네트워크 연결 상태를 검사
    private fun isNetworkAvailable(): Boolean {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val actNw = manager.getNetworkCapabilities(network) ?: return false
        return actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun setupBluetoothAdapter() {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
    }


    //기기가 블루투스를 지원 여부 확인
    private fun isBluetoothSupported() = bluetoothAdapter != null
    //블루투스 활성화 여부를 확인
    private fun checkBluetoothState() {
        if (bluetoothAdapter.isEnabled) {
            showToast("블루투스가 활성화되어 있습니다.")
        } else {
            showToast("블루투스가 비활성화되어 있습니다.")
        }
    }
    //위치 및 블루투스 스캔 권한 요청
    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermissionsIfNeeded() {
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_ACCESS_LOCATION)
        requestPermission(Manifest.permission.BLUETOOTH_SCAN, REQUEST_ENABLE_BLUETOOTH)
        requestPermission(Manifest.permission.BLUETOOTH_CONNECT, REQUEST_BLUETOOTH_CONNECT)
    }

    //권한이 부여된 경우 위치 권한에 따라 블루투스 디바이스 검색을 시작
    private fun requestPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            if (requestCode == REQUEST_ACCESS_LOCATION) {
                startBluetoothDeviceDiscovery()
            }
        }
    }

    //블루투스 스캔 권한이 있는 경우 블루투스 검색
    private fun startBluetoothDeviceDiscovery() {
        if (bluetoothAdapter.isEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
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
            bluetoothAdapter.startDiscovery()
            showToast("기기를 검색합니다.")
        } else {
            showToast("블루투스를 활성화해주세요.")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isReceiverRegistered) {
            registerBluetoothReceiver()
        }
    }
    override fun onPause() {
        super.onPause()
        if (isReceiverRegistered) {
            unregisterReceiver(bluetoothReceiver)
            isReceiverRegistered = false
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) {
            unregisterReceiver(bluetoothReceiver)
            isReceiverRegistered = false
        }
    }

    //블루투스 디바이스 발견 이벤트 처리
    private fun registerBluetoothReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, filter)
        isReceiverRegistered = true
    }
    private val bluetoothReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                val targetMacAddress = "D4:86:60:72:FC:2C"  // 연결하고자 하는 대상 기기의 MAC 주소
                // MAC 주소 확인
                if (device.address == targetMacAddress) {
                    connectToDevice(device)
                }
            }
        }
    }

    //디바이스와 페어링 시도
    @RequiresApi(Build.VERSION_CODES.S)
    private fun connectToDevice(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            if (device.createBond()) {
                showToast("${device.name} (MAC: ${device.address})와 페어링을 시도합니다.")
            } else {
                showToast("페어링 시도 실패")
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_CONNECT)
        }
    }
}