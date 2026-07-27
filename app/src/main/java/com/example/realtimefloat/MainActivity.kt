private val mediaProjectionRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val data = result.data
        // Persist result data and start capture service
        val intent = Intent(this, CaptureForegroundService::class.java)
        intent.putExtra(CaptureForegroundService.EXTRA_RESULT_CODE, result.resultCode)
        intent.putExtra(CaptureForegroundService.EXTRA_RESULT_INTENT, data)
        startForegroundServiceCompat(intent)
    }
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    createNotificationChannel()

    // build a simple downloader VM that uses application context
    val downloadViewModel = ModelDownloadViewModel(applicationContext)

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
Column(modifier = Modifier.fillMaxSize()) {
    Text(text = "Realtime Float - Demo")
    Button(onClick = onRequestCapture) { Text("Start Capture (request MediaProjection)") }
    Text(text = "ASR Engine: $asrEngine")
    Text(text = "Translation Engine: $translationEngine")
    BasicTextField(value = apiKey, onValueChange = { apiKey = it })
    Button(onClick = { /* save API key to prefs - omitted here for brevity */ }) { Text("Save API Key") }
    androidx.compose.material.Row {
        Text(text = "Overlay")
        Switch(checked = overlayEnabled, onCheckedChange = { overlayEnabled = it })
    }
    Button(onClick = {
        // Start floating service as example
        val ctx = (androidx.compose.ui.platform.LocalContext.current) as Context
        val i = Intent(ctx, com.example.realtimefloat.overlay.FloatingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i) else ctx.startService(i)
    }) { Text("Start Floating Overlay") }

    Button(onClick = { showDownloader = !showDownloader }) {
        Text(if (showDownloader) "关闭模型下载" else "打开模型下载")
    }

    if (showDownloader) {
        DownloadScreen(downloadViewModel)
    }
}
