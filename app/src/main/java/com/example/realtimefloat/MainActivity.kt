package com.example.realtimefloat

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.realtimefloat.downloader.DownloadScreen
import com.example.realtimefloat.downloader.ModelDownloadViewModel
import com.example.realtimefloat.service.CaptureForegroundService
import com.example.realtimefloat.overlay.FloatingService

class MainActivity : AppCompatActivity() {

    private val downloadViewModel: ModelDownloadViewModel by viewModels()

    private val mediaProjectionRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val intent = Intent(this, CaptureForegroundService::class.java)
            intent.putExtra(CaptureForegroundService.EXTRA_RESULT_CODE, result.resultCode)
            intent.putExtra(CaptureForegroundService.EXTRA_RESULT_INTENT, data)
            startForegroundServiceCompat(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        setContent {
            MainScreen(onRequestCapture = { requestMediaProjection() }, downloadViewModel = downloadViewModel)
        }
    }

    private fun requestMediaProjection() {
        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mgr.createScreenCaptureIntent()
        mediaProjectionRequest.launch(intent)
    }

    private fun startForegroundServiceCompat(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("rtf_service", "Realtime Capture", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}

@Composable
fun MainScreen(onRequestCapture: () -> Unit, downloadViewModel: ModelDownloadViewModel) {
    val ctx = LocalContext.current
    var asrEngine by remember { mutableStateOf("Whisper-Large-v3") }
    var translationEngine by remember { mutableStateOf("DeepSeek") }
    var apiKey by remember { mutableStateOf("") }
    var overlayEnabled by remember { mutableStateOf(false) }
    var showDownloader by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Realtime Float - Demo")
        Button(onClick = onRequestCapture) { Text("Start Capture (request MediaProjection)") }
        Text(text = "ASR Engine: $asrEngine")
        Text(text = "Translation Engine: $translationEngine")
        BasicTextField(value = apiKey, onValueChange = { apiKey = it })
        Button(onClick = { /* save API key to prefs - omitted here for brevity */ }) { Text("Save API Key") }
        Row {
            Text(text = "Overlay")
            Switch(checked = overlayEnabled, onCheckedChange = { overlayEnabled = it })
        }
        Button(onClick = {
            val i = Intent(ctx, FloatingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i) else ctx.startService(i)
        }) { Text("Start Floating Overlay") }

        Button(onClick = { showDownloader = !showDownloader }) {
            Text(if (showDownloader) "关闭模型下载" else "打开模型下载")
        }

        if (showDownloader) {
            DownloadScreen(downloadViewModel)
        }
    }
}
