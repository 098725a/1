private val _progress = MutableStateFlow<DownloadState>(DownloadState.Idle)
val progress: StateFlow<DownloadState> = _progress

fun startDownload(url: String, modelName: String) {
    if (currentJob != null) return
    val dest = File(context.getExternalFilesDir(null), "models/$modelName")
    dest.parentFile?.mkdirs()
    currentJob = viewModelScope.launch {
        try {
            _progress.value = DownloadState.Preparing
            downloader.download(url, dest) { downloaded, total ->
                _progress.value = DownloadState.Progress(downloaded, total)
            }
            _progress.value = DownloadState.Completed(dest.absolutePath)
        } catch (e: Exception) {
            _progress.value = DownloadState.Error(e.message ?: "download error")
        } finally {
            currentJob = null
        }
    }
}

fun cancelDownload() {
    currentJob?.cancel()
    currentJob = null
    _progress.value = DownloadState.Paused
}
