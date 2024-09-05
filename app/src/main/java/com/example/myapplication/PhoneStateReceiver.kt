package com.example.myapplication

// 03. 전화 수신 시 로그 클래스
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                val incomingNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) // 07. 옵셔널 체이닝
                Log.d("PhoneStateReceiver", "전화가 수신 중입니다. 발신자: $incomingNumber")
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d("PhoneStateReceiver", "통화 중입니다.")
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.d("PhoneStateReceiver", "전화가 종료되었습니다.")
            }
        }
    }
}
