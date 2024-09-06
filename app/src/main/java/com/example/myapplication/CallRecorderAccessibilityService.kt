package com.example.myapplication

import android.util.Log
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.media.MediaRecorder
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


// 12.
class CallRecorderAccessibilityService : AccessibilityService() {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val className = event.className?.toString()
            Log.d("CallAccessibilityService", "Current window: $className")

            // 통화 중인지 확인하는 로직
            if (className?.contains("com.android.incallui") == true) {
                Log.d("CallAccessibilityService", "통화 중인지 확인하는 로직: $isRecording")
                if (!isRecording) {
                    startRecording()
                }
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility 서비스가 중단될 때 호출됨
        Log.d("CallAccessibilityService", "Accessibility 서비스가 중단될 때 호출됨")

    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    private fun startRecording() {
        val outputDir = File(Environment.getExternalStorageDirectory(), "CallRecordings")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".mp3"
        val outputFile = File(outputDir, fileName)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)

            try {
                prepare()
                start()
                isRecording = true
                Log.d("CallAccessibilityService", "Recording started")
            } catch (e: IOException) {
                Log.e("CallAccessibilityService", "Recording failed: ${e.message}")
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            Log.d("CallAccessibilityService", "Recording stopped")
        }
    }
}