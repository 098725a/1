package com.example.realtimefloat.downloader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ModelDownloadViewModel(private val context: Context) : ViewModel() {
    private val downloader = ModelDownloader(context)
    private var currentJob: Job? = null

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
}

sealed class DownloadState {
    object Idle : DownloadState()
    object Preparing : DownloadState()
    data class Progress(val downloaded: Long, val total: Long) : DownloadState()
    data class Completed(val path: String) : DownloadState()
    data class Error(val reason: String) : DownloadState()
    object Paused : DownloadState()
}
