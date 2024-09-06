//package com.example.myapplication
////08. CallScreeningService 클래스 설정
//
//import android.content.Context
//import android.database.Cursor
//import android.net.Uri
//import android.provider.ContactsContract
//import android.telecom.CallScreeningService
//import android.telecom.Call
//import android.util.Log
//
//class MyCallScreeningService : CallScreeningService() {
//
//    override fun onScreenCall(callDetails: Call.Details) {
//        val phoneNumber = callDetails.handle.schemeSpecificPart
//        Log.d("MyCallScreeningService", "전화번호: $phoneNumber")
//
//        // 연락처에서 전화번호로 이름을 확인
//        val contactName = getContactName(phoneNumber, this)
//        if (contactName != null) {
//            Log.d("MyCallScreeningService", "연락처 이름: $contactName")
//        } else {
//            Log.d("MyCallScreeningService", "연락처에 저장되지 않은 번호입니다.")
//        }
//
//        // 차단, 허용 등의 조치를 취할 수 있음
//        respondToCall(callDetails, CallResponse.Builder().build())
//    }
//
//    // 연락처에서 전화번호로 이름 확인
//    private fun getContactName(phoneNumber: String, context: Context): String? {
//        val uri: Uri = Uri.withAppendedPath(
//            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
//            Uri.encode(phoneNumber)
//        )
//        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
//
//        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
//                return it.getString(nameIndex)
//            }
//        }
//        return null
//    }
//}
