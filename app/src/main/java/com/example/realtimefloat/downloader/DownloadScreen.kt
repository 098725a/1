Column(modifier = Modifier.padding(16.dp)) {
    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("模型下载 URL") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = modelName, onValueChange = { modelName = it }, label = { Text("模型存储名") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    Button(onClick = { viewModel.startDownload(url, modelName) }, modifier = Modifier.padding(top = 8.dp)) {
        Text("开始下载/续传")
    }
    Button(onClick = { viewModel.cancelDownload() }, modifier = Modifier.padding(top = 8.dp)) {
        Text("暂停/取消（可恢复）")
    }

    when (state) {
        is DownloadState.Idle -> Text("状态: 空闲", modifier = Modifier.padding(top = 12.dp))
        is DownloadState.Preparing -> {
            Text("准备下载...", modifier = Modifier.padding(top = 12.dp))
            CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
        }
        is DownloadState.Progress -> {
            val p = state as DownloadState.Progress
            val fraction = if (p.total > 0) p.downloaded.toFloat() / p.total else 0f
            Text("下载中: ${p.downloaded}/${p.total}", modifier = Modifier.padding(top = 12.dp))
            LinearProgressIndicator(progress = fraction, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
        is DownloadState.Completed -> {
            val c = state as DownloadState.Completed
            Text("已完成：${c.path}", modifier = Modifier.padding(top = 12.dp))
        }
        is DownloadState.Error -> {
            val e = state as DownloadState.Error
            Text("错误：${e.reason}", modifier = Modifier.padding(top = 12.dp))
        }
        is DownloadState.Paused -> {
            Text("已暂停，可重新开始以续传", modifier = Modifier.padding(top = 12.dp))
        }
    }
}
