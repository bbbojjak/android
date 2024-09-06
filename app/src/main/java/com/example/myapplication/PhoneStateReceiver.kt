package com.example.myapplication

// 03. 전화 수신 시 로그 클래스
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.*
// 현재시간, 딜레이 3초 까지
// http 요청
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
// JSON 변환
import org.json.JSONObject
// 오디오파일 가져오기
import android.database.Cursor
import android.provider.MediaStore
import java.io.File


class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        print("BROADCAST START")
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                Log.d("PhoneStateReceiver", "전화가 수신 중입니다.")
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d("PhoneStateReceiver", "통화 중입니다.")

            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                Log.d("PhoneStateReceiver", "전화가 종료되었습니다.")
                logCurrentTime()
                Log.d("CurrentTime", "3초 딜레이 시작")

                CoroutineScope(Dispatchers.Main).launch {
                    delay(3000) // 3초 대기
                    logCurrentTime()
//                    val number = mapOf<String, String>("name" to "morpheus", "job" to "leader")
//                    var jsonObject = JSONObject(number)

//                    sendPostRequest(urlString = "http://reqres.in/api/users", jsonObject = jsonObject)

                    if (context != null) {
                        uploadFile("http://43.203.42.135:8000/analyze", context= context)
                    }
                }
            }
        }
    }

    fun logCurrentTime() {
        // 현재 시간 가져오기
        val currentTime = LocalDateTime.now()

        // 포맷 지정 (yyyy-MM-dd HH:mm:ss)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // 포맷된 시간 문자열
        val formattedTime = currentTime.format(formatter)

        // Log.d로 출력
        Log.d("CurrentTime", "현재 시간: $formattedTime")
    }


    fun sendPostRequest(urlString: String, jsonObject: JSONObject) {
        Log.d("sendPostRequest", "sendPostRequest: URL 요청 시작")
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        GlobalScope.launch {
            withContext(Dispatchers.IO) {

                try {
                    // 설정
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json; utf-8")
                    connection.setRequestProperty("Accept", "application/json")

                    // JSONObject를 문자열로 변환
                    val jsonString = jsonObject.toString()

                    // 요청 본문 작성
                    connection.outputStream.use { outputStream ->
                        val input = jsonString.toByteArray(Charsets.UTF_8)
                        outputStream.write(input, 0, input.size)
                    }

                    // 응답 코드 및 응답 내용 읽기
                    val responseCode = connection.responseCode
                    val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("sendPostRequest", "Response Code: $responseCode")
                    Log.d("sendPostRequest", "Response Message: $responseMessage")

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    connection.disconnect()
                }
            }
        }


    }

    fun uploadFile(urlString: String, context: Context) {
        Log.d("uploadFile", "Starting file upload")
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        GlobalScope.launch {
            withContext(Dispatchers.IO) {

                try {
                    // Boundary 설정
                    val boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
                    val lineEnd = "\r\n"
                    val twoHyphens = "--"

                    // 설정
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                    // OutputStream을 통해 요청 본문 작성
                    val outputStream: OutputStream = connection.outputStream

//            val contentResolver: ContentResolver = this.contentResolver


                    val file = getAndLogLatestM4aFile(context)
                    if (file?.exists() == true) {
                        val fileName = file.name
                        val fileExtension = file.extension
                        Log.d("LatestM4aFile", "File Path: ${file.absolutePath}")
                        Log.d("LatestM4aFile", "File Name: $fileName")
                        Log.d("LatestM4aFile", "File Extension: $fileExtension")
                        // 추가적인 파일 작업 가능
                    } else {
                        Log.d("LatestM4aFile", "File does not exist.")
                    }
                    // 파일을 읽기 위한 입력 스트림
//            val file = File(filePath)
                    val fileInputStream = file?.inputStream()

                    // 작성할 데이터 구성
                    val requestBody = StringBuilder()
                    requestBody.append(twoHyphens).append(boundary).append(lineEnd)
                    if (file != null) {
                        requestBody.append("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"").append(lineEnd)
                    }
                    requestBody.append("Content-Type: audio/m4a").append(lineEnd)
                    requestBody.append(lineEnd)
                    outputStream.write(requestBody.toString().toByteArray())

                    // 파일 데이터
                    fileInputStream!!.copyTo(outputStream)

                    // 데이터 끝
                    outputStream.write(lineEnd.toByteArray())
                    outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).toByteArray())

                    // 서버 응답 읽기
                    val responseCode = connection.responseCode
                    val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("uploadFile", "Response Code: $responseCode")
                    Log.d("uploadFile", "Response Message: $responseMessage")

                } catch (e: Exception) {
                    Log.e("uploadFile", "Error uploading file", e)
                } finally {
                    connection.disconnect()
                }
            }
        }

    }


    // 가장 최근의 .m4a 파일 가져오는 메서드
    // 가장 최근의 .m4a 파일 경로를 가져오는 메서드
    fun getLatestM4aFilePath(context: Context): String? {
        val contentResolver: ContentResolver = context.contentResolver

        // 오디오 파일을 쿼리하는 URI와 프로젝션 설정
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED // 추가된 날짜를 가져옵니다
        )

        // 쿼리 실행 및 .m4a 확장자로 필터링, 최신 파일 먼저
        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%.m4a")
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

        cursor?.use {
            if (it.moveToFirst()) {
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                return it.getString(dataColumn)
            }
        }
        return null
    }


    fun getAndLogLatestM4aFile(context: Context): File? {
        val latestM4aFilePath = getLatestM4aFilePath(context)
        if (latestM4aFilePath != null) {
            // 파일 객체 생성
            val file = File(latestM4aFilePath)
            if (file.exists()) {
                val fileName = file.name
                val fileExtension = file.extension
                Log.d("LatestM4aFile", "File Path: ${file.absolutePath}")
                Log.d("LatestM4aFile", "File Name: $fileName")
                Log.d("LatestM4aFile", "File Extension: $fileExtension")
                // 추가적인 파일 작업 가능
                return  file
            } else {
                Log.d("LatestM4aFile", "File does not exist.")
                return null
            }
        } else {
            Log.d("LatestM4aFile", "No .m4a files found.")
            return null
        }
    }

}

