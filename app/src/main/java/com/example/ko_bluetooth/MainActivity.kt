package com.example.ko_bluetooth

import android.os.Build
import  com.example.ko_bluetooth.network.BluetoothService
import  com.example.ko_bluetooth.network.NetworkService


import android.os.Bundle
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat



class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothService: BluetoothService
    private lateinit var networkService: NetworkService

    companion object {
        const val REQUEST_ACCESS_LOCATION = 1
        const val REQUEST_ENABLE_BLUETOOTH = 2
        const val REQUEST_BLUETOOTH_CONNECT = 3
        const val REQUEST_BLUETOOTH_SCAN_PERMISSION = 4
        const val REQUEST_ENABLE_BT = 5
    }

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

        bluetoothService = BluetoothService(this)
        networkService = NetworkService(this)

        // 네트워크 상태 확인
        checkNetworkStatus()
        // 블루투스 권한 요청 및 블루투스 활성화 확인
        setupBluetooth()

    }//onCreate

    private fun checkNetworkStatus() {
        if (networkService.isNetworkAvailable()) {
            showToast("네트워크 연결됨")
        } else {
            showToast("네트워크 연결되지 않음")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupBluetooth() {
        if (bluetoothService.isBluetoothSupported()) {
//            showToast("블루투스 사용 가능 기기")

            bluetoothService.requestPermissionsIfNeeded()
        } else {
            showToast("이 기기는 블루투스를 지원하지 않습니다.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


}