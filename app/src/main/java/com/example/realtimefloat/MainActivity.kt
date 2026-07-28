package com.example.realtimefloat.downloader

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModelDownloader(private val context: Context) {
    suspend fun download(url: String, dest: File, onProgress: (Long, Long) -> Unit) {
        withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("Download failed: ${response.code}")
                val contentLength = response.body?.contentLength() ?: -1L
                val inputStream = response.body?.byteStream() ?: throw Exception("No body")
                val outputStream = FileOutputStream(dest)
                inputStream.use { input ->
                    outputStream.use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                onProgress(totalBytesRead, contentLength)
                            }
                        }
                    }
                }
            }
        }
    }
}
