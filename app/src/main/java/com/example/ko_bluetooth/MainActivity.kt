package com.example.ko_bluetooth

import android.Manifest

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat



class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BLUETOOTH = 1
    private val REQUEST_ACCESS_LOCATION = 2

    //액티비티 생명주기 중 하나로, 액티비티가 생성될 때 호출.
    // 이 메소드 내에서 UI를 설정하고, 초기화 작업을 수행
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        // 네트워크 연결 상태 확인
        if (isNetworkAvailable(this)) {
            Toast.makeText(this, "네트워크 연결됨", Toast.LENGTH_LONG).show()
            checkBluetoothState() //블루투스 상태확인

        } else {
            Toast.makeText(this, "네트워크 연결되지 않음", Toast.LENGTH_LONG).show()
        }

        //윈도우 인셋을 적절하게 처리하고 뷰의 패딩을 설정하여 시스템 UI와의 겹침을 방지
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }//onCreate

    //네트워크 연결확인 함수
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    //블루투스 상태확인
    private fun checkBluetoothState() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "블루투스가 활성화되어 있습니다.", Toast.LENGTH_LONG).show()
                searchBluetoothDevices() // 기기 검색
            } else {
                Toast.makeText(this, "블루투스가 비활성화되어 있습니다.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "이 기기는 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show()
        }
    }

    //블루투스 위치권한 확인(물리적 위치정보 유추)
    private fun searchBluetoothDevices() {
        // 위치 권한을 확인하고 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_ACCESS_LOCATION)
        } else {
            // 권한이 있는 경우 Bluetooth 기기를 검색합니다.
            startBluetoothDeviceDiscovery()
            Toast.makeText(this, "기기를 검색합니다.", Toast.LENGTH_LONG).show()

        }
    }

    //블루투스 검색권한 확인
    private fun startBluetoothDeviceDiscovery() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_ACCESS_LOCATION)
            } else {
                bluetoothAdapter.startDiscovery()
            }
        }
    }

    //블루투스 기기 검색
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ACCESS_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 위치 권한이 허용된 경우 Bluetooth 기기 검색을 시작합니다.
                    startBluetoothDeviceDiscovery()
                } else {
                    // 위치 권한이 거부된 경우 사용자에게 메시지를 표시합니다.
                    Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



}