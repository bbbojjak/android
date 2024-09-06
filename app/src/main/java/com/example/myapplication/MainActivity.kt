package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
// 06. 임포트 추가
import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
// 권한 추가
//import android.util.Log
//import android.content.pm.PackageManager
//import androidx.core.content.ContextCompat
//import android.Manifest
//import android.content.ContentResolver
//import android.content.Context
//import android.database.Cursor
//import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import java.io.File


class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 100
    private val REQUEST_READ_CONTACTS = 100
    private val REQUEST_CODE = 101  // 원하는 값으로 설정
    private val TAG = "MainActivity"
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
//        02. 런타임 권한 요청
        // 전화 상태 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSION_REQUEST_CODE)
        }


        // READ_CONTACTS 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 부여되지 않은 경우, 권한을 요청합니다.
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACTS)
        }

        // 14. Accessibility 권한 획득
        requestPermissions()

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "onCreate: 권한이 허용되었습니다.")
//                getAndLogLatestM4aFile(this)

            } else {
                Log.d(TAG, "onCreate: 권한이 거부되었습니다.")
            }
        }

        // 권한 체크
        val 권한 = checkAudioPermission()
        Log.d(TAG, "onCreate: 현재 권한 상황 $권한")

        if (권한) {
//            getAndLogLatestM4aFile(this)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        }
//        if (권한) {
//            getAndLogLatestM4aFile(this)
//        } else {
//            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
//        }
        
        val path = getLatestM4aFilePath(this)
        Log.d(TAG, "onCreate: 파일 주소 : $path")
        
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
    
    
    // 오디오 파일 권한 확인 메서드
    fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }



    //    02-1. 런타임 권한 요청 처리
    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여됨
                Log.d("MainActivity", "READ_PHONE_STATE 권한이 부여되었습니다.")
            } else {
                // 권한이 거부됨
                Log.d("MainActivity", "READ_PHONE_STATE 권한이 거부되었습니다.")
            }
        }

        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되었을 때 녹음 시작 로직 실행
                startRecording()
            } else {
                // 권한이 거부되었을 때 사용자에게 권한 필요성을 다시 안내하거나 기능을 제한
//                Toast.makeText(this, "녹음 권한이 거부되었습니다. 녹음 기능을 사용하려면 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
                Log.d("onRequestPermissionsResult", "startRecording, 녹음 권한이 거부되었습니다. 녹음 기능을 사용하려면 권한을 허용해주세요")

            }
        }
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE
        )
    }

    private fun startRecording() {
        // 녹음 시작 로직 작성
        // 예를 들어 MediaRecorder를 사용하여 녹음을 시작할 수 있습니다.
//        Toast.makeText(this, "녹음이 시작되었습니다.", Toast.LENGTH_SHORT).show()
        Log.d("startRecording", "startRecording, 녹음이 시작되었습니다")

    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}